package com.halo.lims.service;

import com.halo.lims.dto.*;
import com.halo.lims.model.*;
import com.halo.lims.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private SubscriptionPaymentRepository subscriptionPaymentRepository;

    @Autowired
    private RazorpayService razorpayService;

    // ─────────────────────────────────────────────────────────────────────────
    // READ operations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Get all active subscription plans.
     */
    public List<SubscriptionPlanDTO> getAllActivePlans() {
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAllByIsActiveTrue();
        return plans.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Get a single subscription plan by ID.
     */
    public SubscriptionPlanDTO getPlanById(Integer planId) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));
        return convertToDTO(plan);
    }

    /**
     * Get subscription details for an organization (returns null if none exists).
     */
    public SubscriptionDTO getSubscriptionByOrganization(Integer organizationId) {
        return subscriptionRepository.findByOrganizationId(organizationId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Get rich subscription summary (trial status, days remaining, next billing date).
     */
    public SubscriptionSummaryDTO getSubscriptionSummary(Integer organizationId) {
        Subscription subscription = subscriptionRepository.findByOrganizationId(organizationId)
                .orElse(null);

        SubscriptionSummaryDTO summary = new SubscriptionSummaryDTO();
        if (subscription == null) {
            summary.setHasActiveSubscription(false);
            summary.setIsOnTrial(false);
            return summary;
        }

        summary.setStatus(subscription.getStatus());
        summary.setCurrentPlanName(subscription.getPlan().getPlanName());
        summary.setMonthlyAmount(subscription.getMonthlyAmount());
        summary.setDiscountedAmount(subscription.getDiscountedAmount());
        summary.setHasActiveSubscription("ACTIVE".equals(subscription.getStatus())
                || "TRIAL".equals(subscription.getStatus()));

        OffsetDateTime now = OffsetDateTime.now();

        // Trial check
        if (subscription.getTrialEndDate() != null && now.isBefore(subscription.getTrialEndDate())) {
            summary.setIsOnTrial(true);
            long daysRemaining = ChronoUnit.DAYS.between(now, subscription.getTrialEndDate());
            summary.setDaysRemainingInTrial(daysRemaining);
            summary.setExpiryDate(subscription.getTrialEndDate().toString());
        } else {
            summary.setIsOnTrial(false);
        }

        // Next billing date
        if (subscription.getCurrentCycleEnd() != null) {
            long daysUntilBilling = ChronoUnit.DAYS.between(now, subscription.getCurrentCycleEnd());
            summary.setDaysUntilNextBilling(daysUntilBilling);
            summary.setNextBillingDate(subscription.getCurrentCycleEnd().toString());
            if (summary.getExpiryDate() == null || !summary.getIsOnTrial()) {
                summary.setExpiryDate(subscription.getCurrentCycleEnd().toString());
            }
        }

        return summary;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fix #3: Two-phase subscription creation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * PHASE 1 — Initiate: create Razorpay resources (plan/customer/subscription)
     * but do NOT write to our DB yet. Returns only what the frontend needs to
     * open the Razorpay checkout modal.
     *
     * For existing subscriptions (pile-up), returns a flag so the frontend
     * can call /confirm to extend billing locally without a new Razorpay mandate.
     */
    @Transactional
    public InitiateSubscriptionResponse initiateSubscription(Integer organizationId,
                                                              CreateSubscriptionRequest request) {
        log.info("Initiating subscription for organization: {}", organizationId);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found: " + request.getPlanId()));

        Optional<Subscription> existingOpt = subscriptionRepository.findByOrganizationId(organizationId);

        if (existingOpt.isPresent()) {
            Subscription existing = existingOpt.get();
            if ("ACTIVE".equals(existing.getStatus())) {
                // True pile-up: paying customer extending an already-active subscription.
                // No new mandate needed — /confirm will simply extend currentCycleEnd.
                log.info("ACTIVE subscription found for org {}. Pile-up mode — skipping new mandate.", organizationId);
                return InitiateSubscriptionResponse.builder()
                        .razorpayKeyId(razorpayService.getRazorpayKeyId())
                        .razorpaySubscriptionId(null)
                        .planId(plan.getId())
                        .planName(plan.getPlanName())
                        .isNewSubscription(false)
                        .build();
            }
            // For TRIAL / EXPIRED / CANCELLED / PAST_DUE:
            // The existing row is a ghost (unpaid trial or lapsed sub).
            // Create a fresh Razorpay mandate so the user goes through checkout.
            log.info("Existing {} subscription found for org {} — creating fresh Razorpay mandate.",
                    existing.getStatus(), organizationId);
        }

        // New subscription — create full Razorpay mandate
        // Step 1: Ensure Razorpay Plan exists (cached in DB)
        String razorpayPlanId = plan.getRazorpayPlanId();
        if (razorpayPlanId == null || razorpayPlanId.isBlank()) {
            log.info("Creating Razorpay plan for '{}'", plan.getPlanName());
            razorpayPlanId = razorpayService.createRazorpayPlan(plan.getPlanName(), plan.getDiscountedPrice(), "monthly");
            plan.setRazorpayPlanId(razorpayPlanId);
            subscriptionPlanRepository.save(plan);
        }

        // Step 2: Create Razorpay Customer (Fix #7 — use real data from request)
        String razorpayCustomerId = razorpayService.createRazorpayCustomer(
                request.getCustomerName(),
                request.getContactEmail(),
                request.getContactPhone()
        );

        // Step 3: Create Razorpay Subscription (mandate, starts after trial)
        int trialDays = plan.getTrialDays() != null ? plan.getTrialDays() : 7;
        String razorpaySubscriptionId = razorpayService.createRazorpaySubscription(
                razorpayPlanId, razorpayCustomerId, trialDays);

        return InitiateSubscriptionResponse.builder()
                .razorpayKeyId(razorpayService.getRazorpayKeyId())
                .razorpaySubscriptionId(razorpaySubscriptionId)
                .razorpayCustomerId(razorpayCustomerId)
                .razorpayPlanId(razorpayPlanId)
                .planId(plan.getId())
                .planName(plan.getPlanName())
                .isNewSubscription(true)
                .build();
    }

    /**
     * PHASE 2 — Confirm: called after the Razorpay checkout handler fires.
     * Verifies the payment signature and only then persists the subscription to DB.
     * Handles both new subscriptions (TRIAL) and pile-up extensions (ACTIVE).
     */
    @Transactional
    public SubscriptionDTO confirmSubscription(ConfirmSubscriptionRequest request) {
        log.info("Confirming subscription for organization: {}", request.getOrganizationId());

        // Verify payment signature when provided (skip for ACTIVE pile-up with no new payment)
        if (request.getRazorpayPaymentId() != null && request.getRazorpaySubscriptionId() != null
                && request.getRazorpaySignature() != null) {
            boolean valid = razorpayService.verifyPaymentSignature(
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySubscriptionId(),
                    request.getRazorpaySignature());
            if (!valid) {
                throw new RuntimeException("Invalid payment signature — possible tampered request");
            }
            log.info("Payment signature verified for payment: {}", request.getRazorpayPaymentId());
        }

        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found: " + request.getPlanId()));

        Optional<Subscription> existingOpt = subscriptionRepository.findByOrganizationId(request.getOrganizationId());

        if (existingOpt.isPresent()) {
            Subscription existing = existingOpt.get();

            if ("ACTIVE".equals(existing.getStatus())) {
                // Pile-up: extend an already-active paid subscription (no new payment)
                log.info("Extending ACTIVE subscription for org {}", request.getOrganizationId());
                int monthsToAdd = (plan.getBillingMonths() != null && plan.getBillingMonths() > 0)
                        ? plan.getBillingMonths() : 1;
                existing.setPlan(plan);
                existing.setCurrentCycleEnd(existing.getCurrentCycleEnd().plusMonths(monthsToAdd));
                existing.setUpdatedAt(OffsetDateTime.now());
                return convertToDTO(subscriptionRepository.save(existing));
            }

            // TRIAL / EXPIRED / CANCELLED / PAST_DUE:
            // This is a ghost or lapsed record. Update it in-place with the new mandate
            // so we don't violate the UNIQUE constraint on razorpay_subscription_id.
            log.info("Resetting existing {} subscription for org {} with new mandate",
                    existing.getStatus(), request.getOrganizationId());
            int trialDays = plan.getTrialDays() != null ? plan.getTrialDays() : 7;
            OffsetDateTime cycleStart = OffsetDateTime.now();
            OffsetDateTime trialEnd   = cycleStart.plusDays(trialDays);

            existing.setPlan(plan);
            existing.setStatus("TRIAL");
            existing.setRazorpaySubscriptionId(request.getRazorpaySubscriptionId());
            existing.setMonthlyAmount(plan.getPrice());
            existing.setDiscountedAmount(plan.getDiscountedPrice());
            existing.setPaymentMethod(request.getPaymentMethod());
            existing.setAutoRenewal(request.getAutoRenewal() != null ? request.getAutoRenewal() : true);
            existing.setRenewalAttempts(0);
            existing.setTrialEndDate(trialEnd);
            existing.setCurrentCycleStart(cycleStart);
            existing.setCurrentCycleEnd(trialEnd);
            existing.setCancellationReason(null);
            existing.setCancelledAt(null);
            existing.setUpdatedAt(OffsetDateTime.now());

            log.info("Subscription reset to TRIAL for org {}, trial ends {}", request.getOrganizationId(), trialEnd);
            return convertToDTO(subscriptionRepository.save(existing));
        } else {
            // New subscription with 7-day trial
            int trialDays = plan.getTrialDays() != null ? plan.getTrialDays() : 7;
            OffsetDateTime cycleStart = OffsetDateTime.now();
            OffsetDateTime trialEnd = cycleStart.plusDays(trialDays);

            Organization org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new RuntimeException("Organization not found: " + request.getOrganizationId()));

            Subscription subscription = Subscription.builder()
                    .organization(org)
                    .plan(plan)
                    .status("TRIAL")
                    .monthlyAmount(plan.getPrice())
                    .discountedAmount(plan.getDiscountedPrice())
                    .razorpayCustomerId(request.getRazorpaySubscriptionId() != null
                            ? null : null) // customerId not in confirm request; stored via initiate
                    .razorpaySubscriptionId(request.getRazorpaySubscriptionId())
                    .paymentMethod(request.getPaymentMethod())
                    .autoRenewal(request.getAutoRenewal() != null ? request.getAutoRenewal() : true)
                    .renewalAttempts(0)
                    .trialEndDate(trialEnd)
                    .currentCycleStart(cycleStart)
                    .currentCycleEnd(trialEnd)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();

            Subscription saved = subscriptionRepository.save(subscription);
            log.info("New TRIAL subscription created for org {}, expires {}", request.getOrganizationId(), trialEnd);
            return convertToDTO(saved);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Subscription lifecycle operations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Activate subscription after trial ends (can also be called by webhook handler).
     */
    @Transactional
    public void activateSubscription(Integer subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));
        subscription.setStatus("ACTIVE");
        subscription.setCurrentCycleStart(OffsetDateTime.now());
        subscription.setCurrentCycleEnd(OffsetDateTime.now().plusMonths(1));
        subscription.setUpdatedAt(OffsetDateTime.now());
        subscriptionRepository.save(subscription);
        log.info("Subscription {} activated", subscriptionId);
    }

    /**
     * Cancel subscription locally and on Razorpay (graceful: at cycle end).
     */
    @Transactional
    public void cancelSubscription(Integer subscriptionId, String reason) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));

        // Cancel on Razorpay side (graceful — at cycle end, non-blocking)
        if (subscription.getRazorpaySubscriptionId() != null) {
            razorpayService.cancelRazorpaySubscription(subscription.getRazorpaySubscriptionId(), true);
        }

        subscription.setStatus("CANCELLED");
        subscription.setCancellationReason(reason);
        subscription.setCancelledAt(OffsetDateTime.now());
        subscription.setUpdatedAt(OffsetDateTime.now());
        subscriptionRepository.save(subscription);
        log.info("Subscription {} cancelled: {}", subscriptionId, reason);
    }

    /**
     * Record a successful payment and advance the billing cycle.
     * Typically called from the webhook handler on invoice.paid.
     */
    @Transactional
    public SubscriptionPaymentDTO recordPayment(Integer subscriptionId, String paymentId, BigDecimal amount) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));

        SubscriptionPayment payment = SubscriptionPayment.builder()
                .subscription(subscription)
                .razorpayPaymentId(paymentId)
                .status("CAPTURED")
                .amount(amount)
                .currency("INR")
                .paymentDate(OffsetDateTime.now())
                .cycleStart(subscription.getCurrentCycleStart())
                .cycleEnd(subscription.getCurrentCycleEnd())
                .createdAt(OffsetDateTime.now())
                .build();

        SubscriptionPayment savedPayment = subscriptionPaymentRepository.save(payment);

        // Advance billing cycle by billingMonths (Fix #5 + Fix #6)
        int monthsToAdd = (subscription.getPlan().getBillingMonths() != null
                && subscription.getPlan().getBillingMonths() > 0)
                ? subscription.getPlan().getBillingMonths() : 1;

        subscription.setCurrentCycleStart(subscription.getCurrentCycleEnd());
        subscription.setCurrentCycleEnd(subscription.getCurrentCycleEnd().plusMonths(monthsToAdd));
        subscription.setStatus("ACTIVE");
        subscription.setUpdatedAt(OffsetDateTime.now());
        subscriptionRepository.save(subscription);

        log.info("Payment recorded for subscription: {}, advanced cycle by {} month(s)", subscriptionId, monthsToAdd);
        return convertPaymentToDTO(savedPayment);
    }

    /**
     * Mark subscription as PAST_DUE after a failed payment.
     * Called from the webhook handler on invoice.failed.
     */
    @Transactional
    public void markPastDue(String razorpaySubscriptionId) {
        subscriptionRepository.findByRazorpaySubscriptionId(razorpaySubscriptionId).ifPresent(sub -> {
            sub.setStatus("PAST_DUE");
            sub.setRenewalAttempts(sub.getRenewalAttempts() + 1);
            sub.setUpdatedAt(OffsetDateTime.now());
            subscriptionRepository.save(sub);
            log.warn("Subscription {} marked PAST_DUE (attempt {})",
                    sub.getId(), sub.getRenewalAttempts());
        });
    }

    /**
     * Update subscription status based on Razorpay webhook event.
     */
    @Transactional
    public void updateStatusByRazorpayId(String razorpaySubscriptionId, String newStatus) {
        subscriptionRepository.findByRazorpaySubscriptionId(razorpaySubscriptionId).ifPresent(sub -> {
            sub.setStatus(newStatus);
            sub.setUpdatedAt(OffsetDateTime.now());
            subscriptionRepository.save(sub);
            log.info("Subscription {} status updated to {} via webhook", sub.getId(), newStatus);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Payment history
    // ─────────────────────────────────────────────────────────────────────────

    public List<SubscriptionPaymentDTO> getPaymentHistory(Integer subscriptionId) {
        return subscriptionPaymentRepository
                .findBySubscriptionIdOrderByCreatedAtDesc(subscriptionId)
                .stream()
                .map(this::convertPaymentToDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Status helpers
    // ─────────────────────────────────────────────────────────────────────────

    public boolean isSubscriptionActive(Integer organizationId) {
        return subscriptionRepository.findByOrganizationId(organizationId)
                .map(sub -> "ACTIVE".equals(sub.getStatus()) || "TRIAL".equals(sub.getStatus()))
                .orElse(false);
    }

    public boolean isOnTrial(Integer organizationId) {
        return subscriptionRepository.findByOrganizationId(organizationId)
                .map(sub -> sub.getTrialEndDate() != null
                        && OffsetDateTime.now().isBefore(sub.getTrialEndDate()))
                .orElse(false);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Converters
    // ─────────────────────────────────────────────────────────────────────────

    private SubscriptionDTO convertToDTO(Subscription subscription) {
        return SubscriptionDTO.builder()
                .id(subscription.getId())
                .organizationId(subscription.getOrganization().getId())
                .planId(subscription.getPlan().getId())
                .planName(subscription.getPlan().getPlanName())
                .status(subscription.getStatus())
                .currentCycleStart(subscription.getCurrentCycleStart())
                .currentCycleEnd(subscription.getCurrentCycleEnd())
                .trialEndDate(subscription.getTrialEndDate())
                .monthlyAmount(subscription.getMonthlyAmount())
                .discountedAmount(subscription.getDiscountedAmount())
                .paymentMethod(subscription.getPaymentMethod())
                .autoRenewal(subscription.getAutoRenewal())
                .renewalAttempts(subscription.getRenewalAttempts())
                .cancelledAt(subscription.getCancelledAt())
                .razorpaySubscriptionId(subscription.getRazorpaySubscriptionId())
                .razorpayKeyId(razorpayService.getRazorpayKeyId())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }

    private SubscriptionPlanDTO convertToDTO(SubscriptionPlan plan) {
        return SubscriptionPlanDTO.builder()
                .id(plan.getId())
                .planName(plan.getPlanName())
                .price(plan.getPrice())
                .discountedPrice(plan.getDiscountedPrice())
                .discountPercentage(plan.getDiscountPercentage())
                .trialDays(plan.getTrialDays())
                .razorpayPlanId(plan.getRazorpayPlanId())
                .description(plan.getDescription())
                .isActive(plan.getIsActive())
                .maxUsers(plan.getMaxUsers())
                .maxTestsPerMonth(plan.getMaxTestsPerMonth())
                .maxReports(plan.getMaxReports())
                .includesAdvancedAnalytics(plan.getIncludesAdvancedAnalytics())
                .includesCustomBranding(plan.getIncludesCustomBranding())
                .includesApiAccess(plan.getIncludesApiAccess())
                .includesPrioritySupport(plan.getIncludesPrioritySupport())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    private SubscriptionPaymentDTO convertPaymentToDTO(SubscriptionPayment payment) {
        return SubscriptionPaymentDTO.builder()
                .id(payment.getId())
                .subscriptionId(payment.getSubscription().getId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .razorpayInvoiceId(payment.getRazorpayInvoiceId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .transactionFee(payment.getTransactionFee())
                .netAmount(payment.getNetAmount())
                .currency(payment.getCurrency())
                .paymentDate(payment.getPaymentDate())
                .cycleStart(payment.getCycleStart())
                .cycleEnd(payment.getCycleEnd())
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}

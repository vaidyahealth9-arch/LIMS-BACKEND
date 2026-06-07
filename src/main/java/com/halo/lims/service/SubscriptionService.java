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
            summary.setHasUsedTrial(false);
            return summary;
        }

        summary.setHasUsedTrial(true);
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

        int trialDays = plan.getTrialDays() != null ? plan.getTrialDays() : 7;

        if (Boolean.TRUE.equals(request.getSkipTrial())) {
            log.info("Request requested to skip trial for org {}. Setting trialDays = 0", organizationId);
            trialDays = 0;
        }

        if (existingOpt.isPresent()) {
            Subscription existing = existingOpt.get();
            if (existing.getPlan().getId().equals(plan.getId())) {
                if ("ACTIVE".equals(existing.getStatus())) {
                    log.info("Organization {} already has an ACTIVE subscription to the same plan: {}", organizationId, plan.getPlanName());
                    throw new RuntimeException("You already have an active subscription to the " + plan.getPlanName() + " plan. It will auto-renew at the end of the billing cycle.");
                }
                if ("TRIAL".equals(existing.getStatus()) && !Boolean.TRUE.equals(request.getSkipTrial())) {
                    log.info("Organization {} already has a TRIAL subscription to the same plan: {}", organizationId, plan.getPlanName());
                    throw new RuntimeException("You already have a trial subscription to the " + plan.getPlanName() + " plan.");
                }
            }
            log.info("Existing {} subscription found for org {} — creating fresh Razorpay mandate with 0 trial days (one-time trial policy).",
                    existing.getStatus(), organizationId);
            trialDays = 0;
        }

        if (trialDays > 0) {
            log.info("First-time subscription for org {} gets {} trial days. Activating trial directly.", organizationId, trialDays);
            return InitiateSubscriptionResponse.builder()
                    .razorpayKeyId(null)
                    .razorpaySubscriptionId(null)
                    .razorpayOrderId(null)
                    .razorpayCustomerId(null)
                    .razorpayPlanId(null)
                    .planId(plan.getId())
                    .planName(plan.getPlanName())
                    .isNewSubscription(false)
                    .build();
        }

        // Create standard Razorpay Order for renewal/upgrade/downgrade (no e-mandate)
        String razorpayOrderId = null;
        if (razorpayService.isConfigured()) {
            try {
                razorpayOrderId = razorpayService.createRazorpayOrder(plan.getDiscountedPrice());
            } catch (Exception e) {
                log.error("Failed to create Razorpay Order, falling back to offline order", e);
                razorpayOrderId = "mock_order_" + System.currentTimeMillis();
            }
        } else {
            razorpayOrderId = "mock_order_" + System.currentTimeMillis();
        }

        // Create or Re-use Razorpay Customer
        String razorpayCustomerId = null;
        if (existingOpt.isPresent() && existingOpt.get().getRazorpayCustomerId() != null 
                && !existingOpt.get().getRazorpayCustomerId().isBlank()) {
            razorpayCustomerId = existingOpt.get().getRazorpayCustomerId();
            log.info("Reusing existing Razorpay customer ID: {}", razorpayCustomerId);
        } else {
            try {
                if (razorpayService.isConfigured()) {
                    razorpayCustomerId = razorpayService.createRazorpayCustomer(
                            request.getCustomerName(),
                            request.getContactEmail(),
                            request.getContactPhone()
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to create Razorpay Customer: {}", e.getMessage());
            }
        }

        return InitiateSubscriptionResponse.builder()
                .razorpayKeyId(razorpayService.getRazorpayKeyId())
                .razorpaySubscriptionId(null)
                .razorpayOrderId(razorpayOrderId)
                .razorpayCustomerId(razorpayCustomerId)
                .razorpayPlanId(null)
                .planId(plan.getId())
                .planName(plan.getPlanName())
                .isNewSubscription(true)
                .build();
    }

    /**
     * PHASE 2 — Confirm: called after the Razorpay checkout handler fires.
     * Verifies the payment signature and only then persists the subscription to DB.
     * Handles both new subscriptions (TRIAL) and upgrades/downgrades (ACTIVE).
     */
    @Transactional
    public SubscriptionDTO confirmSubscription(ConfirmSubscriptionRequest request) {
        log.info("Confirming subscription for organization: {}", request.getOrganizationId());

        // Verify payment signature when provided
        if (request.getRazorpayPaymentId() != null && request.getRazorpaySignature() != null) {
            boolean valid = false;
            if (request.getRazorpayOrderId() != null && !request.getRazorpayOrderId().isBlank()) {
                valid = razorpayService.verifyOrderSignature(
                        request.getRazorpayPaymentId(),
                        request.getRazorpayOrderId(),
                        request.getRazorpaySignature());
            } else if (request.getRazorpaySubscriptionId() != null && !request.getRazorpaySubscriptionId().isBlank()) {
                valid = razorpayService.verifyPaymentSignature(
                        request.getRazorpayPaymentId(),
                        request.getRazorpaySubscriptionId(),
                        request.getRazorpaySignature());
            }
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

            // Cancel old subscription on Razorpay if it was an e-mandate subscription (legacy compatibility)
            if (existing.getRazorpaySubscriptionId() != null && existing.getRazorpaySubscriptionId().startsWith("sub_")) {
                log.info("Cancelling old Razorpay subscription mandate: {}", existing.getRazorpaySubscriptionId());
                razorpayService.cancelRazorpaySubscription(existing.getRazorpaySubscriptionId(), false);
            }

            int trialDays = 0;

            // Pile up the current validity!
            OffsetDateTime existingEnd = existing.getCurrentCycleEnd();
            OffsetDateTime cycleStart = (existingEnd != null && existingEnd.isAfter(OffsetDateTime.now()))
                    ? existingEnd
                    : OffsetDateTime.now();
            OffsetDateTime trialEnd = null;
            String newStatus = "ACTIVE";

            existing.setPlan(plan);
            existing.setStatus(newStatus);
            
            String transactionRef = request.getRazorpayOrderId() != null && !request.getRazorpayOrderId().isBlank()
                    ? request.getRazorpayOrderId()
                    : request.getRazorpaySubscriptionId();
            existing.setRazorpaySubscriptionId(transactionRef);
            
            if (request.getRazorpayCustomerId() != null && !request.getRazorpayCustomerId().isBlank()) {
                existing.setRazorpayCustomerId(request.getRazorpayCustomerId());
            }
            existing.setMonthlyAmount(plan.getPrice());
            existing.setDiscountedAmount(plan.getDiscountedPrice());
            existing.setPaymentMethod(request.getPaymentMethod());
            existing.setAutoRenewal(false); // No auto-renewal mandate
            existing.setRenewalAttempts(0);
            existing.setTrialEndDate(trialEnd);
            existing.setCurrentCycleStart(OffsetDateTime.now());
            existing.setCurrentCycleEnd(cycleStart); // Temporarily cycleStart so recordPayment can advance it
            existing.setCancellationReason(null);
            existing.setCancelledAt(null);
            existing.setUpdatedAt(OffsetDateTime.now());

            log.info("Subscription updated for org {} with new mandate/order, status {}, trialDays {}, cycleStart starts from {}", 
                    request.getOrganizationId(), newStatus, trialDays, cycleStart);

            Subscription saved = subscriptionRepository.save(existing);

            // If payment ID was returned, record initial payment immediately!
            if (request.getRazorpayPaymentId() != null) {
                log.info("Recording initial payment for upgrade/downgrade: {}", request.getRazorpayPaymentId());
                recordPayment(saved.getId(), request.getRazorpayPaymentId(), plan.getDiscountedPrice());
            }

            return convertToDTO(subscriptionRepository.findById(saved.getId()).get());
        } else {
            // New subscription
            boolean paid = request.getRazorpayPaymentId() != null;
            int trialDays = paid ? 0 : (plan.getTrialDays() != null ? plan.getTrialDays() : 7);
            
            OffsetDateTime cycleStart = OffsetDateTime.now();
            OffsetDateTime trialEnd = trialDays > 0 ? cycleStart.plusDays(trialDays) : null;
            String newStatus = trialDays > 0 ? "TRIAL" : "ACTIVE";

            Organization org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new RuntimeException("Organization not found: " + request.getOrganizationId()));

            String transactionRef = request.getRazorpayOrderId() != null && !request.getRazorpayOrderId().isBlank()
                    ? request.getRazorpayOrderId()
                    : request.getRazorpaySubscriptionId();

            Subscription subscription = Subscription.builder()
                    .organization(org)
                    .plan(plan)
                    .status(newStatus)
                    .monthlyAmount(plan.getPrice())
                    .discountedAmount(plan.getDiscountedPrice())
                    .razorpayCustomerId(request.getRazorpayCustomerId())
                    .razorpaySubscriptionId(transactionRef)
                    .paymentMethod(request.getPaymentMethod())
                    .autoRenewal(false) // No auto-renewal mandate
                    .renewalAttempts(0)
                    .trialEndDate(trialEnd)
                    .currentCycleStart(cycleStart)
                    .currentCycleEnd(trialDays > 0 ? trialEnd : cycleStart) // Set temporarily so recordPayment can advance it
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();

            Subscription saved = subscriptionRepository.save(subscription);
            log.info("New {} subscription created for org {}, expires {}", newStatus, request.getOrganizationId(), saved.getCurrentCycleEnd());

            if (paid) {
                log.info("Recording initial payment for new subscription: {}", request.getRazorpayPaymentId());
                recordPayment(saved.getId(), request.getRazorpayPaymentId(), plan.getDiscountedPrice());
            }

            return convertToDTO(subscriptionRepository.findById(saved.getId()).get());
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
        
        int monthsToAdd = (subscription.getPlan().getBillingMonths() != null
                && subscription.getPlan().getBillingMonths() > 0)
                ? subscription.getPlan().getBillingMonths() : 1;

        subscription.setStatus("ACTIVE");
        subscription.setCurrentCycleStart(OffsetDateTime.now());
        subscription.setCurrentCycleEnd(OffsetDateTime.now().plusMonths(monthsToAdd));
        subscription.setUpdatedAt(OffsetDateTime.now());
        subscriptionRepository.save(subscription);
        log.info("Subscription {} activated with {} month(s)", subscriptionId, monthsToAdd);
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
     * Record a payment using Razorpay subscription ID (called from webhook).
     */
    @Transactional
    public void recordPaymentByRazorpayId(String razorpaySubscriptionId, String paymentId, BigDecimal amount) {
        subscriptionRepository.findByRazorpaySubscriptionId(razorpaySubscriptionId).ifPresentOrElse(sub -> {
            log.info("Found subscription {} for Razorpay subscription ID {}. Recording payment.", sub.getId(), razorpaySubscriptionId);
            recordPayment(sub.getId(), paymentId, amount);
        }, () -> {
            log.error("Subscription not found for Razorpay subscription ID: {}", razorpaySubscriptionId);
            throw new RuntimeException("Subscription not found for Razorpay subscription ID: " + razorpaySubscriptionId);
        });
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

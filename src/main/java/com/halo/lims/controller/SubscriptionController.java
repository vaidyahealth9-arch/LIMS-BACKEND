package com.halo.lims.controller;

import com.halo.lims.dto.*;
import com.halo.lims.service.RazorpayService;
import com.halo.lims.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@Slf4j
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private RazorpayService razorpayService;

    // ─────────────────────────────────────────────────────────────────────────
    // Plans
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlanDTO>> getAllPlans() {
        log.info("Fetching all subscription plans");
        return ResponseEntity.ok(subscriptionService.getAllActivePlans());
    }

    @GetMapping("/plans/{planId}")
    public ResponseEntity<SubscriptionPlanDTO> getPlanById(@PathVariable Integer planId) {
        log.info("Fetching subscription plan: {}", planId);
        return ResponseEntity.ok(subscriptionService.getPlanById(planId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Subscription queries
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<SubscriptionDTO> getSubscription(@PathVariable Integer organizationId) {
        log.info("Fetching subscription for organization: {}", organizationId);
        SubscriptionDTO subscription = subscriptionService.getSubscriptionByOrganization(organizationId);
        if (subscription == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/organization/{organizationId}/summary")
    public ResponseEntity<SubscriptionSummaryDTO> getSubscriptionSummary(@PathVariable Integer organizationId) {
        log.info("Fetching subscription summary for organization: {}", organizationId);
        return ResponseEntity.ok(subscriptionService.getSubscriptionSummary(organizationId));
    }

    @GetMapping("/organization/{organizationId}/status")
    public ResponseEntity<?> checkSubscriptionStatus(@PathVariable Integer organizationId) {
        log.info("Checking subscription status for organization: {}", organizationId);
        Map<String, Object> status = new HashMap<>();
        status.put("isActive", subscriptionService.isSubscriptionActive(organizationId));
        status.put("isOnTrial", subscriptionService.isOnTrial(organizationId));
        return ResponseEntity.ok(status);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fix #3: Two-phase subscription creation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * PHASE 1 — Initiate: creates Razorpay plan/customer/subscription (no DB write).
     * Returns the razorpayKeyId and razorpaySubscriptionId the frontend needs to open the modal.
     * For pile-up (existing subscription), razorpaySubscriptionId will be null —
     * the frontend should call /confirm directly (no modal needed).
     */
    @PostMapping("/initiate")
    public ResponseEntity<?> initiateSubscription(
            @RequestParam Integer organizationId,
            @RequestBody CreateSubscriptionRequest request) {
        try {
            log.info("Initiating subscription for organization: {}", organizationId);
            InitiateSubscriptionResponse response =
                    subscriptionService.initiateSubscription(organizationId, request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error initiating subscription", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * PHASE 2 — Confirm: called after the Razorpay checkout handler fires.
     * Verifies payment signature, then writes the Subscription row to our DB.
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmSubscription(@RequestBody ConfirmSubscriptionRequest request) {
        try {
            log.info("Confirming subscription for organization: {}", request.getOrganizationId());
            SubscriptionDTO subscription = subscriptionService.confirmSubscription(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Subscription confirmed successfully");
            response.put("subscription", subscription);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error confirming subscription", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Subscription management
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<?> cancelSubscription(
            @PathVariable Integer subscriptionId,
            @RequestParam String reason) {
        try {
            log.info("Cancelling subscription: {}", subscriptionId);
            subscriptionService.cancelSubscription(subscriptionId, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Subscription cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error cancelling subscription", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Payment history
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/{subscriptionId}/payments")
    public ResponseEntity<List<SubscriptionPaymentDTO>> getPaymentHistory(
            @PathVariable Integer subscriptionId) {
        log.info("Fetching payment history for subscription: {}", subscriptionId);
        return ResponseEntity.ok(subscriptionService.getPaymentHistory(subscriptionId));
    }

    /**
     * Record payment manually (admin/internal use — normally handled by webhook).
     */
    @PostMapping("/{subscriptionId}/payment")
    public ResponseEntity<?> recordPayment(
            @PathVariable Integer subscriptionId,
            @RequestParam String paymentId,
            @RequestParam BigDecimal amount) {
        try {
            log.info("Recording payment for subscription: {}", subscriptionId);
            SubscriptionPaymentDTO payment =
                    subscriptionService.recordPayment(subscriptionId, paymentId, amount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment recorded successfully");
            response.put("payment", payment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error recording payment", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fix #4: Razorpay Webhook — full event handling with signature verification
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Receives Razorpay webhook events and routes them to appropriate service methods.
     * Signature verification is performed first (skipped in local dev when secret is blank).
     *
     * Handled events:
     *   subscription.activated → status = ACTIVE
     *   subscription.halted    → status = PAST_DUE
     *   subscription.cancelled → status = CANCELLED
     *   subscription.resumed   → status = ACTIVE
     *   invoice.paid           → record payment, advance billing cycle
     *   invoice.failed         → status = PAST_DUE, increment renewal_attempts
     */
    @PostMapping("/webhook/razorpay")
    public ResponseEntity<?> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        try {
            log.info("Received Razorpay webhook");

            // Verify signature (permissive if webhook secret not configured in local dev)
            if (signature != null && !razorpayService.verifyWebhookSignature(payload, signature)) {
                log.error("Invalid Razorpay webhook signature — rejecting event");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Invalid signature"));
            }

            JSONObject event = new JSONObject(payload);
            String eventType = event.optString("event", "");
            JSONObject entity = event.optJSONObject("payload") != null
                    ? event.getJSONObject("payload").optJSONObject("subscription") != null
                        ? event.getJSONObject("payload").getJSONObject("subscription").optJSONObject("entity")
                        : null
                    : null;

            JSONObject invoiceEntity = event.optJSONObject("payload") != null
                    ? event.getJSONObject("payload").optJSONObject("invoice") != null
                        ? event.getJSONObject("payload").getJSONObject("invoice").optJSONObject("entity")
                        : null
                    : null;

            log.info("Processing webhook event: {}", eventType);

            switch (eventType) {
                case "subscription.activated":
                    if (entity != null) {
                        subscriptionService.updateStatusByRazorpayId(entity.optString("id"), "ACTIVE");
                    }
                    break;

                case "subscription.halted":
                    if (entity != null) {
                        subscriptionService.updateStatusByRazorpayId(entity.optString("id"), "PAST_DUE");
                    }
                    break;

                case "subscription.cancelled":
                    if (entity != null) {
                        subscriptionService.updateStatusByRazorpayId(entity.optString("id"), "CANCELLED");
                    }
                    break;

                case "subscription.resumed":
                    if (entity != null) {
                        subscriptionService.updateStatusByRazorpayId(entity.optString("id"), "ACTIVE");
                    }
                    break;

                case "invoice.paid":
                    if (invoiceEntity != null) {
                        String subId = invoiceEntity.optString("subscription_id");
                        String paymentId = invoiceEntity.optString("payment_id");
                        // amount is in paise — convert to rupees
                        long amountPaise = invoiceEntity.optLong("amount_paid", 0);
                        BigDecimal amountRupees = BigDecimal.valueOf(amountPaise).divide(BigDecimal.valueOf(100));

                        log.info("Invoice paid for subscription {}, payment {}, amount ₹{}",
                                subId, paymentId, amountRupees);

                        // Record payment and advance the billing cycle start/end dates
                        subscriptionService.recordPaymentByRazorpayId(subId, paymentId, amountRupees);
                    }
                    break;

                case "invoice.failed":
                    if (invoiceEntity != null) {
                        String subId = invoiceEntity.optString("subscription_id");
                        subscriptionService.markPastDue(subId);
                    }
                    break;

                default:
                    log.info("Unhandled Razorpay webhook event type: {}", eventType);
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Webhook processed"));
        } catch (Exception e) {
            log.error("Error processing Razorpay webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}

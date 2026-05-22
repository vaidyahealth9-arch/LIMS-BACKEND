package com.halo.lims.service;

import com.razorpay.Customer;
import com.razorpay.Plan;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Subscription;
import com.razorpay.Utils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;

@Service
@Slf4j
public class RazorpayService {

    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:}")
    private String razorpayKeySecret;

    @Value("${razorpay.webhook.secret:}")
    private String razorpayWebhookSecret;

    private RazorpayClient razorpayClient;

    // Improvement A: Graceful degradation — don't crash if keys are missing
    @PostConstruct
    public void init() {
        if (isConfigured()) {
            try {
                this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
                log.info("RazorpayService initialized successfully.");
            } catch (RazorpayException e) {
                log.error("Error initializing Razorpay client: {}", e.getMessage());
            }
        } else {
            log.warn("Razorpay credentials are not configured — payment features will be unavailable. " +
                     "Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET environment variables.");
        }
    }

    /**
     * Returns true only when both key_id and key_secret are non-blank.
     * Gate all payment operations behind this check.
     */
    public boolean isConfigured() {
        return razorpayKeyId != null && !razorpayKeyId.isBlank()
                && razorpayKeySecret != null && !razorpayKeySecret.isBlank();
    }

    /**
     * Create a Razorpay plan (if not already created for this plan)
     */
    public String createRazorpayPlan(String planName, BigDecimal amount, String period) {
        if (!isConfigured()) throw new RuntimeException("Razorpay not configured");
        try {
            JSONObject planRequest = new JSONObject();
            planRequest.put("period", period.toLowerCase()); // monthly, yearly, etc.
            planRequest.put("interval", 1);
            planRequest.put("item", new JSONObject()
                    .put("name", planName)
                    .put("amount", amount.multiply(new BigDecimal("100")).longValue()) // in paise
                    .put("currency", "INR")
                    .put("description", planName));

            Plan plan = razorpayClient.plans.create(planRequest);
            return plan.get("id");
        } catch (RazorpayException e) {
            log.error("Error creating Razorpay plan: {}", e.getMessage());
            throw new RuntimeException("Failed to create Razorpay plan", e);
        }
    }

    /**
     * Create a Razorpay customer
     */
    public String createRazorpayCustomer(String customerName, String email, String phone) {
        if (!isConfigured()) throw new RuntimeException("Razorpay not configured");
        try {
            JSONObject customerRequest = new JSONObject();
            customerRequest.put("name", customerName);
            customerRequest.put("email", email);
            if (phone != null && !phone.isBlank()) {
                customerRequest.put("contact", phone);
            }
            Customer customer = razorpayClient.customers.create(customerRequest);
            return customer.get("id");
        } catch (RazorpayException e) {
            log.error("Error creating Razorpay customer: {}", e.getMessage());
            throw new RuntimeException("Failed to create Razorpay customer", e);
        }
    }

    /**
     * Create a Razorpay subscription (UPI Autopay / Mandate).
     * start_at is pushed forward by trialDays so the first charge happens post-trial.
     */
    public String createRazorpaySubscription(String planId, String customerId, int trialDays) {
        if (!isConfigured()) throw new RuntimeException("Razorpay not configured");
        try {
            JSONObject subscriptionRequest = new JSONObject();
            subscriptionRequest.put("plan_id", planId);
            subscriptionRequest.put("customer_id", customerId);
            subscriptionRequest.put("total_count", 12); // 12 billing cycles
            subscriptionRequest.put("quantity", 1);
            subscriptionRequest.put("customer_notify", 1);

            long startAt = (System.currentTimeMillis() / 1000) + ((long) trialDays * 24 * 60 * 60);
            subscriptionRequest.put("start_at", startAt);

            Subscription subscription = razorpayClient.subscriptions.create(subscriptionRequest);
            return subscription.get("id");
        } catch (RazorpayException e) {
            log.error("Error creating Razorpay subscription: {}", e.getMessage());
            throw new RuntimeException("Failed to create Razorpay subscription", e);
        }
    }

    /**
     * Verify Razorpay webhook signature (HMAC-SHA256).
     * Skip verification when webhook secret is not configured (local dev with no webhook).
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        if (razorpayWebhookSecret == null || razorpayWebhookSecret.isBlank()) {
            log.warn("Razorpay webhook secret not configured — skipping signature check (dev mode)");
            return true; // permissive in local dev
        }
        try {
            return Utils.verifyWebhookSignature(payload, signature, razorpayWebhookSecret);
        } catch (Exception e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verify Razorpay payment signature after checkout handler fires.
     * HMAC-SHA256(payment_id + "|" + subscription_id, key_secret) must match received signature.
     */
    public boolean verifyPaymentSignature(String paymentId, String subscriptionId, String receivedSignature) {
        try {
            JSONObject params = new JSONObject();
            params.put("razorpay_payment_id", paymentId);
            params.put("razorpay_subscription_id", subscriptionId);
            params.put("razorpay_signature", receivedSignature);
            Utils.verifyPaymentSignature(params, razorpayKeySecret);
            return true;
        } catch (RazorpayException e) {
            log.warn("Payment signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Cancel a subscription on Razorpay's side.
     * cancelAtCycleEnd=true → cancel at end of billing cycle (graceful); false → immediate.
     */
    public void cancelRazorpaySubscription(String razorpaySubscriptionId, boolean cancelAtCycleEnd) {
        if (!isConfigured()) {
            log.warn("Razorpay not configured — skipping remote cancellation for {}", razorpaySubscriptionId);
            return;
        }
        try {
            JSONObject cancelRequest = new JSONObject();
            cancelRequest.put("cancel_at_cycle_end", cancelAtCycleEnd ? 1 : 0);
            razorpayClient.subscriptions.cancel(razorpaySubscriptionId, cancelRequest);
            log.info("Cancelled Razorpay subscription: {}", razorpaySubscriptionId);
        } catch (RazorpayException e) {
            // Log but don't re-throw: DB-side cancellation still proceeds
            log.error("Error cancelling Razorpay subscription {}: {}", razorpaySubscriptionId, e.getMessage());
        }
    }

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }
}

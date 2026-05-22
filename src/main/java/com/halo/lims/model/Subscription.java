package com.halo.lims.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "subscriptions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Column(name = "subscription_status", nullable = false, length = 50)
    private String status; // ACTIVE, INACTIVE, CANCELLED, EXPIRED, TRIAL, PAST_DUE

    @Column(name = "current_cycle_start")
    private OffsetDateTime currentCycleStart;

    @Column(name = "current_cycle_end")
    private OffsetDateTime currentCycleEnd;

    @Column(name = "trial_end_date")
    private OffsetDateTime trialEndDate;

    @Column(name = "razorpay_subscription_id", unique = true, length = 255)
    private String razorpaySubscriptionId;

    @Column(name = "razorpay_customer_id", length = 255)
    private String razorpayCustomerId;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal monthlyAmount;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal discountedAmount; // Amount after discount

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // card, netbanking, wallet, upi

    @Column(name = "auto_renewal", nullable = false)
    private Boolean autoRenewal; // Whether subscription auto-renews

    @Column(name = "renewal_attempts")
    private Integer renewalAttempts;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

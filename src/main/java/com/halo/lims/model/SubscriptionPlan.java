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
@Table(name = "subscription_plans")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String planName; // e.g., "Basic", "Professional", "Enterprise"

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal price; // Monthly price in INR

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal discountedPrice; // Price after discount

    @Column(nullable = false)
    private Integer discountPercentage; // e.g., 75

    @Column(nullable = false)
    private Integer trialDays; // e.g., 7 days free trial

    @Column(name = "razorpay_plan_id", length = 255)
    private String razorpayPlanId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(name = "billing_cycle", length = 50)
    private String billingCycle; // e.g., "MONTHLY", "QUARTERLY", "SIX_MONTHLY"

    @Column(name = "billing_months", nullable = false)
    private Integer billingMonths = 1; // Number of months per billing cycle (1, 3, or 6)

    @Column(name = "max_users")
    private Integer maxUsers; // Maximum users allowed

    @Column(name = "max_tests_per_month")
    private Integer maxTestsPerMonth; // Maximum tests per month

    @Column(name = "max_reports")
    private Integer maxReports; // Unlimited or limited

    @Column(name = "includes_advanced_analytics")
    private Boolean includesAdvancedAnalytics;

    @Column(name = "includes_custom_branding")
    private Boolean includesCustomBranding;

    @Column(name = "includes_api_access")
    private Boolean includesApiAccess;

    @Column(name = "includes_priority_support")
    private Boolean includesPrioritySupport;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

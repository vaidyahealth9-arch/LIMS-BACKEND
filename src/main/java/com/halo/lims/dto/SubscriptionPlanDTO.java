package com.halo.lims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SubscriptionPlanDTO {
    private Integer id;
    private String planName;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private Integer discountPercentage;
    private Integer trialDays;
    private String razorpayPlanId;
    private String description;
    private Boolean isActive;
    private Integer maxUsers;
    private Integer maxTestsPerMonth;
    private Integer maxReports;
    private Boolean includesAdvancedAnalytics;
    private Boolean includesCustomBranding;
    private Boolean includesApiAccess;
    private Boolean includesPrioritySupport;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

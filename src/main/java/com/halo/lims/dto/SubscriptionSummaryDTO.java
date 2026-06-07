package com.halo.lims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SubscriptionSummaryDTO {
    private Boolean isOnTrial;
    private Boolean hasActiveSubscription;
    private String currentPlanName;
    private BigDecimal monthlyAmount;
    private BigDecimal discountedAmount;
    private String status;
    private Long daysRemainingInTrial;
    private Long daysUntilNextBilling;
    private String nextBillingDate;
    private String expiryDate;
    private Boolean hasUsedTrial;
}

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
public class SubscriptionDTO {
    private Integer id;
    private Integer organizationId;
    private Integer planId;
    private String planName;
    private String status;
    private OffsetDateTime currentCycleStart;
    private OffsetDateTime currentCycleEnd;
    private OffsetDateTime trialEndDate;
    private BigDecimal monthlyAmount;
    private BigDecimal discountedAmount;
    private String paymentMethod;
    private Boolean autoRenewal;
    private Integer renewalAttempts;
    private OffsetDateTime cancelledAt;
    private String razorpaySubscriptionId;
    private String razorpayKeyId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

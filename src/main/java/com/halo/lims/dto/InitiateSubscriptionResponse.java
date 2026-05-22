package com.halo.lims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class InitiateSubscriptionResponse {
    private String razorpayKeyId;
    private String razorpaySubscriptionId;
    private String razorpayCustomerId;
    private String razorpayPlanId;
    private Integer planId;
    private String planName;
    private boolean isNewSubscription; // true = new org, false = pile-up/extend
}

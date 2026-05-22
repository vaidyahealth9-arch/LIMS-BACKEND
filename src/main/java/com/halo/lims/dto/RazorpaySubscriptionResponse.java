package com.halo.lims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RazorpaySubscriptionResponse {
    private String subscriptionId;
    private String subscriptionLink;
    private String planId;
    private String customerId;
    private String status;
    private Long currentCycleEnd;
    private Long trialEnd;
}

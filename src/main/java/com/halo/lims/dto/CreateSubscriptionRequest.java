package com.halo.lims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CreateSubscriptionRequest {
    private Integer planId;
    private String paymentMethod; // card, netbanking, wallet, upi
    private Boolean autoRenewal; // Default: true
    private String contactEmail;
    private String contactPhone;
    private String customerName;
}

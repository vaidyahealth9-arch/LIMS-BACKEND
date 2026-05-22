package com.halo.lims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ConfirmSubscriptionRequest {
    private Integer organizationId;
    private Integer planId;

    // Customer details (stored on Subscription for reference)
    private String customerName;
    private String contactEmail;
    private String contactPhone;
    private String paymentMethod;
    private Boolean autoRenewal;

    // Razorpay payment response fields (from handler callback)
    private String razorpayPaymentId;
    private String razorpaySubscriptionId;
    private String razorpaySignature;
}

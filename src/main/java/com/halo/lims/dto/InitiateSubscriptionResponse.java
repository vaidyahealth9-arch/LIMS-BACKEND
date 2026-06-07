package com.halo.lims.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String razorpayOrderId;
    private Integer planId;
    private String planName;

    private boolean isNewSubscription; // true = new org, false = pile-up/extend

    @JsonProperty("isNewSubscription")
    public boolean isNewSubscription() {
        return isNewSubscription;
    }

    @JsonProperty("isNewSubscription")
    public void setNewSubscription(boolean isNewSubscription) {
        this.isNewSubscription = isNewSubscription;
    }
}

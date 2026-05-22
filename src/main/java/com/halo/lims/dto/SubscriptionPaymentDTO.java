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
public class SubscriptionPaymentDTO {
    private Integer id;
    private Integer subscriptionId;
    private String razorpayPaymentId;
    private String razorpayInvoiceId;
    private String status;
    private BigDecimal amount;
    private BigDecimal transactionFee;
    private BigDecimal netAmount;
    private String currency;
    private OffsetDateTime paymentDate;
    private OffsetDateTime cycleStart;
    private OffsetDateTime cycleEnd;
    private String notes;
    private OffsetDateTime createdAt;
}

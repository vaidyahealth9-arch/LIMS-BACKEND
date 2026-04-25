package com.halo.lims.dto.billing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class BillPaymentRequest {
    @NotBlank(message = "Payment method is required")
    @Size(max = 50)
    private String paymentMethod; 

    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount paid must be positive")
    private BigDecimal amountPaid;

    private OffsetDateTime paymentDate; 

    public BillPaymentRequest() {}

    // Getters and Setters
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public OffsetDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(OffsetDateTime paymentDate) { this.paymentDate = paymentDate; }
}

package com.halo.lims.dto.billing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class BillPaymentRequest {
    @NotBlank(message = "Payment method is required")
    @Size(max = 50)
    private String paymentMethod; // e.g., "CASH", "UPI", "CARD"

    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount paid must be positive")
    private BigDecimal amountPaid;

    private OffsetDateTime paymentDate; // Defaults to now if not provided
}

package com.halo.lims.dto.billing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class BillCreateRequest {
    @NotNull(message = "Encounter ID is required")
    @Min(value = 1, message = "Encounter ID must be positive")
    private Integer encounterId;

    @NotNull(message = "At least one Service Request ID is required for the bill")
    private List<Integer> serviceRequestIds; // List of ServiceRequest IDs to include in this bill

    @DecimalMin(value = "0.0", message = "Discount percentage must be non-negative")
    private BigDecimal discountPercentage = BigDecimal.ZERO; // Default 0%

    @Size(max = 50)
    private String initialPaymentMethod; // e.g., "CASH", "UPI", "CARD", "NONE" (if fully due)

    @DecimalMin(value = "0.0", message = "Initial paid amount must be non-negative")
    private BigDecimal initialPaidAmount = BigDecimal.ZERO;

    private String notes;
    private OffsetDateTime dueDate;
}

package com.halo.lims.dto.organization.test;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrganizationTestRequest {
    @NotNull(message = "Test ID is required")
    private Integer testId;

    @NotNull(message = "Is enabled status is required")
    private Boolean isEnabled;

    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price; // Optional: Organization-specific price
}

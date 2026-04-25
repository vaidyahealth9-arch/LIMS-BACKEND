package com.halo.lims.dto.organization.test;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class OrganizationTestRequest {
    @NotNull(message = "Test ID is required")
    private Integer testId;

    @NotNull(message = "Is enabled status is required")
    private Boolean isEnabled;

    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price; // Optional: Organization-specific price

    public OrganizationTestRequest() {}

    // Getters and Setters
    public Integer getTestId() { return testId; }
    public void setTestId(Integer testId) { this.testId = testId; }

    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}

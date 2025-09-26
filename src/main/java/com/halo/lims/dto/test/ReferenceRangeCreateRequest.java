package com.halo.lims.dto.test;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReferenceRangeCreateRequest {
    @NotNull(message = "Analyte ID is required")
    @Min(value = 1, message = "Analyte ID must be positive")
    private Integer analyteId;

    @Pattern(regexp = "male|female|other|unknown|null", message = "Gender must be male, female, other, unknown, or null")
    private String gender; // null for all genders

    @Min(value = 0, message = "Minimum age must be non-negative")
    private Integer minAgeYears;

    @Min(value = 0, message = "Maximum age must be non-negative")
    private Integer maxAgeYears;

    @DecimalMin(value = "0.0", inclusive = false, message = "Low value must be positive")
    private BigDecimal lowValue;

    @DecimalMin(value = "0.0", inclusive = false, message = "High value must be positive")
    private BigDecimal highValue;

    @Size(max = 255)
    private String textRange;

    @Size(max = 50)
    private String interpretationCode; // e.g., "N", "H", "L"
}

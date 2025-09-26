package com.halo.lims.dto.test;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TestAnalyteCreateRequest {
    @NotBlank(message = "Analyte code is required")
    @Size(max = 100)
    private String analyteCode;

    @NotBlank(message = "Analyte name is required")
    @Size(max = 255)
    private String analyteName;

    @NotNull(message = "Parent Test ID is required")
    @Min(value = 1, message = "Parent Test ID must be positive")
    private Integer parentTestId;

    @Size(max = 50)
    private String loincCode;

    @Size(max = 255)
    private String loincSystem; // Default to 'http://loinc.org'

    private Integer unitId;

    @NotBlank(message = "Result type is required")
    @Pattern(regexp = "Numeric|Text|Coded", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Result type must be Numeric, Text, or Coded")
    private String resultType;

    private Integer decimalPlaces;

    private String biologicalRefInterval;

    private Boolean isDerived = false;

    private String formula;
}

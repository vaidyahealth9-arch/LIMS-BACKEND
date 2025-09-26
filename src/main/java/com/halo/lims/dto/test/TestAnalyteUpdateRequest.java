package com.halo.lims.dto.test;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TestAnalyteUpdateRequest {
    @Size(max = 255)
    private String analyteName;

    @Size(max = 50)
    private String loincCode;

    @Size(max = 255)
    private String loincSystem;

    private Integer unitId;

    @Pattern(regexp = "Numeric|Text|Coded", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Result type must be Numeric, Text, or Coded")
    private String resultType;

    private Integer decimalPlaces;

    private String biologicalRefInterval;

    private Boolean isDerived;

    private String formula;
}

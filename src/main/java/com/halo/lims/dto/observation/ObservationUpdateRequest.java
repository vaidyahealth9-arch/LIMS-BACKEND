package com.halo.lims.dto.observation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ObservationUpdateRequest {

    // Only one of these should be provided based on Analyte's result_type
    @DecimalMin(value = "0.0", inclusive = false, message = "Numeric value must be positive")
    private BigDecimal valueNumeric;

    @Size(max = 2000, message = "String value cannot exceed 2000 characters")
    private String valueString;

    @Size(max = 255, message = "Coded value cannot exceed 255 characters")
    private String valueCode;

    @Size(max = 255, message = "Coded value system cannot exceed 255 characters")
    private String valueCodeSystem;

    @Size(max = 255, message = "Interpretation code cannot exceed 50 characters")
    private String interpretationCode;

    @Size(max = 255, message = "Interpretation system cannot exceed 255 characters")
    private String interpretationSystem;

    private OffsetDateTime effectiveDateTime;
}

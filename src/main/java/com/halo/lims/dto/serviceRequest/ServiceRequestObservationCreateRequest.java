package com.halo.lims.dto.serviceRequest;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ServiceRequestObservationCreateRequest {

    private Integer specimenId;

    @NotNull(message = "Analyte ID is required")
    @Min(value = 1, message = "Analyte ID must be positive")
    private Integer analyteId;

    // Only one of these should be provided based on Analyte's result_type
    @DecimalMin(value = "0.0", inclusive = false, message = "Numeric value must be positive")
    private BigDecimal valueNumeric;

    @Size(max = 2000, message = "String value cannot exceed 2000 characters")
    private String valueString;

    @Size(max = 255, message = "Coded value cannot exceed 255 characters")
    private String valueCode; // For coded results like "Positive", "Negative"

    @Size(max = 255, message = "Coded value system cannot exceed 255 characters")
    private String valueCodeSystem; // e.g., for SNOMED CT codes

    @Size(max = 255, message = "Interpretation code cannot exceed 50 characters")
    private String interpretationCode; // Optional, can be auto-calculated

    @Size(max = 255, message = "Interpretation system cannot exceed 255 characters")
    private String interpretationSystem = "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation";

    private OffsetDateTime effectiveDateTime;
}

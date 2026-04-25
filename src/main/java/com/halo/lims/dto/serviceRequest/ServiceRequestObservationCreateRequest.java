package com.halo.lims.dto.serviceRequest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class ServiceRequestObservationCreateRequest {

    private Integer specimenId;

    @NotNull(message = "Analyte ID is required")
    @Min(value = 1, message = "Analyte ID must be positive")
    private Integer analyteId;

    @PositiveOrZero(message = "Numeric value must be non-negative")
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
    private String interpretationSystem = "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation";

    private OffsetDateTime effectiveDateTime;

    public ServiceRequestObservationCreateRequest() {}

    // Getters and Setters
    public Integer getSpecimenId() { return specimenId; }
    public void setSpecimenId(Integer specimenId) { this.specimenId = specimenId; }

    public Integer getAnalyteId() { return analyteId; }
    public void setAnalyteId(Integer analyteId) { this.analyteId = analyteId; }

    public BigDecimal getValueNumeric() { return valueNumeric; }
    public void setValueNumeric(BigDecimal valueNumeric) { this.valueNumeric = valueNumeric; }

    public String getValueString() { return valueString; }
    public void setValueString(String valueString) { this.valueString = valueString; }

    public String getValueCode() { return valueCode; }
    public void setValueCode(String valueCode) { this.valueCode = valueCode; }

    public String getValueCodeSystem() { return valueCodeSystem; }
    public void setValueCodeSystem(String valueCodeSystem) { this.valueCodeSystem = valueCodeSystem; }

    public String getInterpretationCode() { return interpretationCode; }
    public void setInterpretationCode(String interpretationCode) { this.interpretationCode = interpretationCode; }

    public String getInterpretationSystem() { return interpretationSystem; }
    public void setInterpretationSystem(String interpretationSystem) { this.interpretationSystem = interpretationSystem; }

    public OffsetDateTime getEffectiveDateTime() { return effectiveDateTime; }
    public void setEffectiveDateTime(OffsetDateTime effectiveDateTime) { this.effectiveDateTime = effectiveDateTime; }
}

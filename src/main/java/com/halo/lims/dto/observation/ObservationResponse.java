package com.halo.lims.dto.observation;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ObservationResponse {
    private Integer id;
    private String localObservationValue;
    private Integer serviceRequestId;
    private String patientMrn; // Denormalized for convenience
    private String analyteName;
    private String testName; // Parent test name
    private String specimenLocalValue;
    private BigDecimal valueNumeric;
    private String valueString;
    private String valueCode;
    private String unitName;
    private String interpretationCode;
    private String status;
    private String performerName; // Name of the technician/pathologist
    private OffsetDateTime effectiveDateTime;
    private OffsetDateTime issuedDateTime;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
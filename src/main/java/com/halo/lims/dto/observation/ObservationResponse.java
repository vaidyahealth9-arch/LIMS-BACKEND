package com.halo.lims.dto.observation;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class ObservationResponse {
    private String id;
    private String serviceRequestId;
    private String specimenId;
    private String testName;
    private String analyteId;
    private String analyteName;
    private BigDecimal valueNumeric;
    private String valueString;
    private String unit;
    private String referenceRange;
    private String interpretation;
    private OffsetDateTime effectiveDateTime;
}

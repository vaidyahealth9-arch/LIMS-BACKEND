package com.halo.lims.dto.test;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ReferenceRangeResponse {
    private Integer id;
    private Integer analyteId;
    private String analyteCode;
    private String analyteName;
    private String gender;
    private Integer minAgeYears;
    private Integer maxAgeYears;
    private BigDecimal lowValue;
    private BigDecimal highValue;
    private String textRange;
    private String interpretationCode;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

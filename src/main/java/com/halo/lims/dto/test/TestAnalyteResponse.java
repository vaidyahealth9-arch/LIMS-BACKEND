package com.halo.lims.dto.test;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class TestAnalyteResponse {
    private Integer id;
    private String analyteCode;
    private String analyteName;
    private Integer parentTestId;
    private String parentTestLocalCode; // From parent Test
    private String parentTestName; // From parent Test
    private String loincCode;
    private String loincSystem;
    private Integer unitId;
    private String unitName;
    private String resultType;
    private Integer decimalPlaces;
    private String biologicalRefInterval;
    private Boolean isDerived;
    private String formula;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

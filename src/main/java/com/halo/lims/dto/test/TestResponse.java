package com.halo.lims.dto.test;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class TestResponse {
    private Integer id;
    private String testName;
    private String localCode;
    private String loincCode;
    private String loincSystem;
    private String department;
    private String containerDescription;
    private String method;
    private String measuringPrinciple;
    private String turnAroundTimeText;
    private String reflexProfileText;
    private String reportNotes;
    private Integer organizationId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

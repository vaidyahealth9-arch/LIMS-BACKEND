package com.halo.lims.dto.test;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TestUpdateRequest {
    @Size(max = 255)
    private String testName;

    // localCode is usually immutable, so not included in update request

    @Size(max = 50)
    private String loincCode;

    @Size(max = 255)
    private String loincSystem;

    @Size(max = 100)
    private String department;

    private String containerDescription;

    @Size(max = 255)
    private String method;

    private String measuringPrinciple;

    @Size(max = 255)
    private String turnAroundTimeText;

    private String reflexProfileText;

    private String reportNotes;
}
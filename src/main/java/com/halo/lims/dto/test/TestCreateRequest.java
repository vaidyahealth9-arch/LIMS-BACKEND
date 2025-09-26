package com.halo.lims.dto.test;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TestCreateRequest {
    @NotBlank(message = "Test name is required")
    @Size(max = 255)
    private String testName;

    @NotBlank(message = "Local code is required")
    @Size(max = 100)
    private String localCode; // e.g., "HEM-APTT"

    @Size(max = 50)
    private String loincCode;

    @Size(max = 255)
    private String loincSystem; // Default to 'http://loinc.org'

    @Size(max = 100)
    private String department;

    private String containerDescription; // TEXT field, no @Size max

    @Size(max = 255)
    private String method;

    private String measuringPrinciple; // TEXT field

    @Size(max = 255)
    private String turnAroundTimeText;

    private String reflexProfileText; // TEXT field

    private String reportNotes; // TEXT field
}
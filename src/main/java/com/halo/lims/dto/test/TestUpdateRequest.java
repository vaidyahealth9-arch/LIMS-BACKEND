package com.halo.lims.dto.test;

import jakarta.validation.constraints.Size;

public class TestUpdateRequest {
    private Integer organizationId;

    @Size(max = 255)
    private String testName;

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

    public TestUpdateRequest() {}

    // Getters and Setters
    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getLoincCode() { return loincCode; }
    public void setLoincCode(String loincCode) { this.loincCode = loincCode; }

    public String getLoincSystem() { return loincSystem; }
    public void setLoincSystem(String loincSystem) { this.loincSystem = loincSystem; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getContainerDescription() { return containerDescription; }
    public void setContainerDescription(String containerDescription) { this.containerDescription = containerDescription; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getMeasuringPrinciple() { return measuringPrinciple; }
    public void setMeasuringPrinciple(String measuringPrinciple) { this.measuringPrinciple = measuringPrinciple; }

    public String getTurnAroundTimeText() { return turnAroundTimeText; }
    public void setTurnAroundTimeText(String turnAroundTimeText) { this.turnAroundTimeText = turnAroundTimeText; }

    public String getReflexProfileText() { return reflexProfileText; }
    public void setReflexProfileText(String reflexProfileText) { this.reflexProfileText = reflexProfileText; }

    public String getReportNotes() { return reportNotes; }
    public void setReportNotes(String reportNotes) { this.reportNotes = reportNotes; }
}
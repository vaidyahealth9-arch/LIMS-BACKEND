package com.halo.lims.dto.test;

import java.time.OffsetDateTime;

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

    public TestResponse() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getLocalCode() { return localCode; }
    public void setLocalCode(String localCode) { this.localCode = localCode; }

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

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

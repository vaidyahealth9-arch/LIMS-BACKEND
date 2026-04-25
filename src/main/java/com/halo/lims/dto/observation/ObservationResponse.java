package com.halo.lims.dto.observation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

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
    private String comments;
    private OffsetDateTime effectiveDateTime;

    public ObservationResponse() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getServiceRequestId() { return serviceRequestId; }
    public void setServiceRequestId(String serviceRequestId) { this.serviceRequestId = serviceRequestId; }

    public String getSpecimenId() { return specimenId; }
    public void setSpecimenId(String specimenId) { this.specimenId = specimenId; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getAnalyteId() { return analyteId; }
    public void setAnalyteId(String analyteId) { this.analyteId = analyteId; }

    public String getAnalyteName() { return analyteName; }
    public void setAnalyteName(String analyteName) { this.analyteName = analyteName; }

    public BigDecimal getValueNumeric() { return valueNumeric; }
    public void setValueNumeric(BigDecimal valueNumeric) { this.valueNumeric = valueNumeric; }

    public String getValueString() { return valueString; }
    public void setValueString(String valueString) { this.valueString = valueString; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getReferenceRange() { return referenceRange; }
    public void setReferenceRange(String referenceRange) { this.referenceRange = referenceRange; }

    public String getInterpretation() { return interpretation; }
    public void setInterpretation(String interpretation) { this.interpretation = interpretation; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public OffsetDateTime getEffectiveDateTime() { return effectiveDateTime; }
    public void setEffectiveDateTime(OffsetDateTime effectiveDateTime) { this.effectiveDateTime = effectiveDateTime; }
}

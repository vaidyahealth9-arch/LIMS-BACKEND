package com.halo.lims.dto.test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

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

    public ReferenceRangeResponse() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getAnalyteId() { return analyteId; }
    public void setAnalyteId(Integer analyteId) { this.analyteId = analyteId; }

    public String getAnalyteCode() { return analyteCode; }
    public void setAnalyteCode(String analyteCode) { this.analyteCode = analyteCode; }

    public String getAnalyteName() { return analyteName; }
    public void setAnalyteName(String analyteName) { this.analyteName = analyteName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getMinAgeYears() { return minAgeYears; }
    public void setMinAgeYears(Integer minAgeYears) { this.minAgeYears = minAgeYears; }

    public Integer getMaxAgeYears() { return maxAgeYears; }
    public void setMaxAgeYears(Integer maxAgeYears) { this.maxAgeYears = maxAgeYears; }

    public BigDecimal getLowValue() { return lowValue; }
    public void setLowValue(BigDecimal lowValue) { this.lowValue = lowValue; }

    public BigDecimal getHighValue() { return highValue; }
    public void setHighValue(BigDecimal highValue) { this.highValue = highValue; }

    public String getTextRange() { return textRange; }
    public void setTextRange(String textRange) { this.textRange = textRange; }

    public String getInterpretationCode() { return interpretationCode; }
    public void setInterpretationCode(String interpretationCode) { this.interpretationCode = interpretationCode; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

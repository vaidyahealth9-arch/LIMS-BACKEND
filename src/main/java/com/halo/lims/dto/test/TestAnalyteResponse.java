package com.halo.lims.dto.test;

import java.time.OffsetDateTime;

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
    private Integer organizationId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public TestAnalyteResponse() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getAnalyteCode() { return analyteCode; }
    public void setAnalyteCode(String analyteCode) { this.analyteCode = analyteCode; }

    public String getAnalyteName() { return analyteName; }
    public void setAnalyteName(String analyteName) { this.analyteName = analyteName; }

    public Integer getParentTestId() { return parentTestId; }
    public void setParentTestId(Integer parentTestId) { this.parentTestId = parentTestId; }

    public String getParentTestLocalCode() { return parentTestLocalCode; }
    public void setParentTestLocalCode(String parentTestLocalCode) { this.parentTestLocalCode = parentTestLocalCode; }

    public String getParentTestName() { return parentTestName; }
    public void setParentTestName(String parentTestName) { this.parentTestName = parentTestName; }

    public String getLoincCode() { return loincCode; }
    public void setLoincCode(String loincCode) { this.loincCode = loincCode; }

    public String getLoincSystem() { return loincSystem; }
    public void setLoincSystem(String loincSystem) { this.loincSystem = loincSystem; }

    public Integer getUnitId() { return unitId; }
    public void setUnitId(Integer unitId) { this.unitId = unitId; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getResultType() { return resultType; }
    public void setResultType(String resultType) { this.resultType = resultType; }

    public Integer getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(Integer decimalPlaces) { this.decimalPlaces = decimalPlaces; }

    public String getBiologicalRefInterval() { return biologicalRefInterval; }
    public void setBiologicalRefInterval(String biologicalRefInterval) { this.biologicalRefInterval = biologicalRefInterval; }

    public Boolean getIsDerived() { return isDerived; }
    public void setIsDerived(Boolean isDerived) { this.isDerived = isDerived; }

    public String getFormula() { return formula; }
    public void setFormula(String formula) { this.formula = formula; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

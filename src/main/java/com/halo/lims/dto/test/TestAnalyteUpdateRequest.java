package com.halo.lims.dto.test;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TestAnalyteUpdateRequest {
    @Size(max = 255)
    private String analyteName;

    @Size(max = 50)
    private String loincCode;

    @Size(max = 255)
    private String loincSystem;

    private Integer unitId;

    @Pattern(regexp = "Numeric|Text|Coded", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Result type must be Numeric, Text, or Coded")
    private String resultType;

    private Integer decimalPlaces;

    private String biologicalRefInterval;

    private Boolean isDerived;

    private String formula;

    public TestAnalyteUpdateRequest() {}

    // Getters and Setters
    public String getAnalyteName() { return analyteName; }
    public void setAnalyteName(String analyteName) { this.analyteName = analyteName; }

    public String getLoincCode() { return loincCode; }
    public void setLoincCode(String loincCode) { this.loincCode = loincCode; }

    public String getLoincSystem() { return loincSystem; }
    public void setLoincSystem(String loincSystem) { this.loincSystem = loincSystem; }

    public Integer getUnitId() { return unitId; }
    public void setUnitId(Integer unitId) { this.unitId = unitId; }

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
}

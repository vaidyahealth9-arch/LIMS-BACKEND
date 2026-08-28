package com.halo.lims.dto.test;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TestAnalyteCreateRequest {
    private Integer organizationId;

    @NotBlank(message = "Analyte code is required")
    @Size(max = 100)
    private String analyteCode;

    @NotBlank(message = "Analyte name is required")
    @Size(max = 255)
    private String analyteName;

    @NotNull(message = "Parent Test ID is required")
    @Min(value = 1, message = "Parent Test ID must be positive")
    private Integer parentTestId;

    @Size(max = 50)
    private String loincCode;

    @Size(max = 255)
    private String loincSystem; // Default to 'http://loinc.org'

    private Integer unitId;

    @NotBlank(message = "Result type is required")
    @Pattern(regexp = "Numeric|Text|Coded", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Result type must be Numeric, Text, or Coded")
    private String resultType;

    private Integer decimalPlaces;

    private String biologicalRefInterval;

    private Boolean isDerived = false;

    private String formula;

    public TestAnalyteCreateRequest() {}

    // Getters and Setters
    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public String getAnalyteCode() { return analyteCode; }
    public void setAnalyteCode(String analyteCode) { this.analyteCode = analyteCode; }

    public String getAnalyteName() { return analyteName; }
    public void setAnalyteName(String analyteName) { this.analyteName = analyteName; }

    public Integer getParentTestId() { return parentTestId; }
    public void setParentTestId(Integer parentTestId) { this.parentTestId = parentTestId; }

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

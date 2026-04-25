package com.halo.lims.dto.organizationTestAnalyte;

public class OrganizationTestAnalyteDto {
    private Integer organizationId;
    private Integer testAnalyteId;
    private String resultType;
    private Integer decimalPlaces;
    private String biologicalRefInterval;

    public OrganizationTestAnalyteDto() {}

    // Getters and Setters
    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public Integer getTestAnalyteId() { return testAnalyteId; }
    public void setTestAnalyteId(Integer testAnalyteId) { this.testAnalyteId = testAnalyteId; }

    public String getResultType() { return resultType; }
    public void setResultType(String resultType) { this.resultType = resultType; }

    public Integer getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(Integer decimalPlaces) { this.decimalPlaces = decimalPlaces; }

    public String getBiologicalRefInterval() { return biologicalRefInterval; }
    public void setBiologicalRefInterval(String biologicalRefInterval) { this.biologicalRefInterval = biologicalRefInterval; }
}

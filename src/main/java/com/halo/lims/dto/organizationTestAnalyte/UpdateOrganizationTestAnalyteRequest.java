package com.halo.lims.dto.organizationTestAnalyte;

public class UpdateOrganizationTestAnalyteRequest {
    private String resultType;
    private Integer decimalPlaces;
    private String biologicalRefInterval;

    public UpdateOrganizationTestAnalyteRequest() {}

    // Getters and Setters
    public String getResultType() { return resultType; }
    public void setResultType(String resultType) { this.resultType = resultType; }

    public Integer getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(Integer decimalPlaces) { this.decimalPlaces = decimalPlaces; }

    public String getBiologicalRefInterval() { return biologicalRefInterval; }
    public void setBiologicalRefInterval(String biologicalRefInterval) { this.biologicalRefInterval = biologicalRefInterval; }
}

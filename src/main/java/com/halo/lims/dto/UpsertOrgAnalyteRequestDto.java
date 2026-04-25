package com.halo.lims.dto;

public class UpsertOrgAnalyteRequestDto {
    private Integer analyteId;
    private Double price;
    private String code;
    private String bioReference;

    public UpsertOrgAnalyteRequestDto() {}

    // Getters and Setters
    public Integer getAnalyteId() { return analyteId; }
    public void setAnalyteId(Integer analyteId) { this.analyteId = analyteId; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getBioReference() { return bioReference; }
    public void setBioReference(String bioReference) { this.bioReference = bioReference; }
}

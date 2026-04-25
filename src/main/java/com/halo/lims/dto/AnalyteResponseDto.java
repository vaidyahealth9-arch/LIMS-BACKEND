package com.halo.lims.dto;

public class AnalyteResponseDto {
    private Long id;
    private String name;
    private Double price;
    private String code;
    private String associatedTest;
    private String bioReference;
    private boolean isOrgSpecific;

    public AnalyteResponseDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getAssociatedTest() { return associatedTest; }
    public void setAssociatedTest(String associatedTest) { this.associatedTest = associatedTest; }

    public String getBioReference() { return bioReference; }
    public void setBioReference(String bioReference) { this.bioReference = bioReference; }

    public boolean isOrgSpecific() { return isOrgSpecific; }
    public void setOrgSpecific(boolean isOrgSpecific) { this.isOrgSpecific = isOrgSpecific; }
}

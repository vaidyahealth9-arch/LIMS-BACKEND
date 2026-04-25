package com.halo.lims.dto.organization;

import java.time.OffsetDateTime;

public class OrganizationResponse {
    private Integer id;
    private String organizationName;
    private String orgType;
    private String contactPhone;
    private String contactEmail;
    private String addressLine1;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String abdmFacilityId;
    private String gstin;
    private String localIdentifierValue;
    private String reportHeaderImage;
    private String reportFooterImage;
    private Integer reportHeaderMarginMm;
    private Integer reportFooterMarginMm;
    private Integer reportHeaderHeightMm;
    private Integer reportFooterHeightMm;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public OrganizationResponse() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getOrgType() { return orgType; }
    public void setOrgType(String orgType) { this.orgType = orgType; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAbdmFacilityId() { return abdmFacilityId; }
    public void setAbdmFacilityId(String abdmFacilityId) { this.abdmFacilityId = abdmFacilityId; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public String getLocalIdentifierValue() { return localIdentifierValue; }
    public void setLocalIdentifierValue(String localIdentifierValue) { this.localIdentifierValue = localIdentifierValue; }

    public String getReportHeaderImage() { return reportHeaderImage; }
    public void setReportHeaderImage(String reportHeaderImage) { this.reportHeaderImage = reportHeaderImage; }

    public String getReportFooterImage() { return reportFooterImage; }
    public void setReportFooterImage(String reportFooterImage) { this.reportFooterImage = reportFooterImage; }

    public Integer getReportHeaderMarginMm() { return reportHeaderMarginMm; }
    public void setReportHeaderMarginMm(Integer reportHeaderMarginMm) { this.reportHeaderMarginMm = reportHeaderMarginMm; }

    public Integer getReportFooterMarginMm() { return reportFooterMarginMm; }
    public void setReportFooterMarginMm(Integer reportFooterMarginMm) { this.reportFooterMarginMm = reportFooterMarginMm; }

    public Integer getReportHeaderHeightMm() { return reportHeaderHeightMm; }
    public void setReportHeaderHeightMm(Integer reportHeaderHeightMm) { this.reportHeaderHeightMm = reportHeaderHeightMm; }

    public Integer getReportFooterHeightMm() { return reportFooterHeightMm; }
    public void setReportFooterHeightMm(Integer reportFooterHeightMm) { this.reportFooterHeightMm = reportFooterHeightMm; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

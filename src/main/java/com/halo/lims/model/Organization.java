package com.halo.lims.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "organizations")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @Column(name = "org_type", nullable = false, length = 100)
    private String orgType; // e.g., "laboratory", "hospital"

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 100)
    private String country; // Default 'IND'

    @Column(name = "abdm_facility_id", length = 255)
    private String abdmFacilityId;

    @Column(name = "gstin", length = 15)
    private String gstin;

    @Column(name = "local_identifier_system", nullable = false, length = 255)
    private String localIdentifierSystem;

    @Column(name = "local_identifier_value", unique = true, nullable = false, length = 255)
    private String localIdentifierValue;

    @Column(name = "report_header_image", columnDefinition = "TEXT")
    private String reportHeaderImage;

    @Column(name = "report_footer_image", columnDefinition = "TEXT")
    private String reportFooterImage;

    @Column(name = "header_image_asset_id")
    private Integer headerImageAssetId;

    @Column(name = "footer_image_asset_id")
    private Integer footerImageAssetId;

    @Column(name = "report_header_margin_mm")
    private Integer reportHeaderMarginMm;

    @Column(name = "report_footer_margin_mm")
    private Integer reportFooterMarginMm;

    @Column(name = "report_header_height_mm")
    private Integer reportHeaderHeightMm;

    @Column(name = "report_footer_height_mm")
    private Integer reportFooterHeightMm;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

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

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

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

    public String getLocalIdentifierSystem() { return localIdentifierSystem; }
    public void setLocalIdentifierSystem(String localIdentifierSystem) { this.localIdentifierSystem = localIdentifierSystem; }

    public String getLocalIdentifierValue() { return localIdentifierValue; }
    public void setLocalIdentifierValue(String localIdentifierValue) { this.localIdentifierValue = localIdentifierValue; }

    public String getReportHeaderImage() { return reportHeaderImage; }
    public void setReportHeaderImage(String reportHeaderImage) { this.reportHeaderImage = reportHeaderImage; }

    public String getReportFooterImage() { return reportFooterImage; }
    public void setReportFooterImage(String reportFooterImage) { this.reportFooterImage = reportFooterImage; }

    public Integer getHeaderImageAssetId() { return headerImageAssetId; }
    public void setHeaderImageAssetId(Integer headerImageAssetId) { this.headerImageAssetId = headerImageAssetId; }

    public Integer getFooterImageAssetId() { return footerImageAssetId; }
    public void setFooterImageAssetId(Integer footerImageAssetId) { this.footerImageAssetId = footerImageAssetId; }

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
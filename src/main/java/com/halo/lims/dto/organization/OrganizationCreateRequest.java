package com.halo.lims.dto.organization;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class OrganizationCreateRequest {
    @NotBlank(message = "Organization name is required")
    @Size(max = 255)
    private String organizationName;

    @NotBlank(message = "Organization type is required")
    @Pattern(regexp = "laboratory|hospital|referring_clinic|government_agency", message = "Invalid organization type")
    private String orgType;

    @Size(max = 50)
    private String contactPhone;

    @Email(message = "Email ID must be a valid email format")
    @Size(max = 100)
    private String contactEmail;

    @Size(max = 255)
    private String addressLine1;

    @Size(max = 255)
    private String addressLine2;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 100)
    private String country = "IND"; // Default to India

    @Size(max = 255)
    private String abdmFacilityId; // Health Facility Registry ID

    @Pattern(
        regexp = "^$|^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$",
        message = "GSTIN must be a valid 15-character Indian GSTIN"
    )
    private String gstin;

    @NotBlank(message = "Local identifier value is required (e.g., your internal lab code)")
    @Size(max = 255)
    private String localIdentifierValue; // Your internal ID for this organization

    public OrganizationCreateRequest() {}

    // Getters and Setters
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

    public String getLocalIdentifierValue() { return localIdentifierValue; }
    public void setLocalIdentifierValue(String localIdentifierValue) { this.localIdentifierValue = localIdentifierValue; }
}

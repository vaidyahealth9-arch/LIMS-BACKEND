package com.halo.lims.dto.organization;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
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

    @NotBlank(message = "Local identifier value is required (e.g., your internal lab code)")
    @Size(max = 255)
    private String localIdentifierValue; // Your internal ID for this organization
}

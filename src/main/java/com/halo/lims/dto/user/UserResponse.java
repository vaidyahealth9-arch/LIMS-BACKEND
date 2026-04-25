package com.halo.lims.dto.user;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

public class UserResponse {
    private Integer id;
    private String username;
    private Set<String> roles;
    private Boolean isActive;
    private Integer organizationId;
    private String organizationName;

    private Integer practitionerId;
    private String practitionerLocalIdentifierValue;
    private String practitionerFirstName;
    private String practitionerLastName;
    private String practitionerGender;
    private LocalDate practitionerDateOfBirth;
    private String practitionerSignatureImage;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public UserResponse() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public Integer getPractitionerId() { return practitionerId; }
    public void setPractitionerId(Integer practitionerId) { this.practitionerId = practitionerId; }

    public String getPractitionerLocalIdentifierValue() { return practitionerLocalIdentifierValue; }
    public void setPractitionerLocalIdentifierValue(String practitionerLocalIdentifierValue) { this.practitionerLocalIdentifierValue = practitionerLocalIdentifierValue; }

    public String getPractitionerFirstName() { return practitionerFirstName; }
    public void setPractitionerFirstName(String practitionerFirstName) { this.practitionerFirstName = practitionerFirstName; }

    public String getPractitionerLastName() { return practitionerLastName; }
    public void setPractitionerLastName(String practitionerLastName) { this.practitionerLastName = practitionerLastName; }

    public String getPractitionerGender() { return practitionerGender; }
    public void setPractitionerGender(String practitionerGender) { this.practitionerGender = practitionerGender; }

    public LocalDate getPractitionerDateOfBirth() { return practitionerDateOfBirth; }
    public void setPractitionerDateOfBirth(LocalDate practitionerDateOfBirth) { this.practitionerDateOfBirth = practitionerDateOfBirth; }

    public String getPractitionerSignatureImage() { return practitionerSignatureImage; }
    public void setPractitionerSignatureImage(String practitionerSignatureImage) { this.practitionerSignatureImage = practitionerSignatureImage; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
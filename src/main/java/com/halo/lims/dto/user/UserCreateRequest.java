package com.halo.lims.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

public class UserCreateRequest {

    @NotBlank(message = "First name is required for practitioner profile")
    @Size(max = 100)
    private String practitionerFirstName;

    @Size(max = 100)
    private String practitionerLastName;

    @Pattern(regexp = "male|female|other|unknown", message = "Gender must be male, female, other, or unknown")
    private String practitionerGender;

    private LocalDate practitionerDateOfBirth;

    private String practitionerSignatureImage;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 100, message = "Username must be between 4 and 100 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password; 

    @NotNull(message = "At least one role is required")
    private Set<String> roles; 

    @NotNull(message = "Organization ID is required")
    private Integer organizationId;

    private Boolean isActive = true;

    public UserCreateRequest() {}

    // Getters and Setters
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

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
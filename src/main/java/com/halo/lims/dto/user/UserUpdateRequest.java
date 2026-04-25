package com.halo.lims.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

public class UserUpdateRequest {
    @Size(max = 100)
    private String practitionerFirstName;

    @Size(max = 100)
    private String practitionerLastName;

    @Pattern(regexp = "male|female|other|unknown", message = "Gender must be male, female, other, or unknown")
    private String practitionerGender;

    private LocalDate practitionerDateOfBirth;

    private String practitionerSignatureImage;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword; 

    private Set<String> roles; 

    private Boolean isActive; 

    public UserUpdateRequest() {}

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

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

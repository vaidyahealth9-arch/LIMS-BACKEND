package com.halo.lims.dto.user;


import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserUpdateRequest {
    // Practitioner Details (can be updated)
    @Size(max = 100)
    private String practitionerFirstName;

    @Size(max = 100)
    private String practitionerLastName;

    @Pattern(regexp = "male|female|other|unknown", message = "Gender must be male, female, other, or unknown")
    private String practitionerGender;

    private LocalDate practitionerDateOfBirth;

    // User Login Details
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword; // Optional, only if changing password

    private Set<String> roles; // Can update roles

    private Boolean isActive; // Toggle access
}

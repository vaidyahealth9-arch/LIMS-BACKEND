package com.halo.lims.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserCreateRequest {

    // Practitioner Details (for the Practitioner profile linked to this user)
    @NotBlank(message = "First name is required for practitioner profile")
    @Size(max = 100)
    private String practitionerFirstName;

    @Size(max = 100)
    private String practitionerLastName;

    @Pattern(regexp = "male|female|other|unknown", message = "Gender must be male, female, other, or unknown")
    private String practitionerGender;

    private LocalDate practitionerDateOfBirth;

    // User Login Details
    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 100, message = "Username must be between 4 and 100 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password; // Will be hashed by service

    @NotNull(message = "At least one role is required")
    private Set<String> roles; // e.g., ["TECHNICIAN", "PATHOLOGIST", "ADMIN", "MANAGER"]

    @NotNull(message = "Organization ID is required")
    private Integer organizationId;

    private Boolean isActive = true;
}
package com.halo.lims.dto.patient;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import javax.annotation.Nullable;
import java.time.LocalDate;

@Data
public class PatientRegistrationRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Size(max = 100, message = "Middle name must not exceed 100 characters")
    private String middleName;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "male|female|other|unknown", message = "Gender must be male, female, other, or unknown")
    private String gender;

    @NotNull(message = "Date of birth is required")
    @PastOrPresent(message = "Date of birth cannot be in the future")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String contactPhone; // This will be encrypted in DB

    @Email(message = "Email ID must be a valid email format")
    @Size(max = 100, message = "Email ID must not exceed 100 characters")
    private String contactEmail; // This will be encrypted in DB

    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String addressLine1; // This will be encrypted in DB

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    private String addressLine2; // This will be encrypted in DB

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    // For ABHA Integration (Initial creation/linking)
    @Pattern(regexp = "^$|^[0-9]{12}$", message = "Aadhaar number must be 12 digits if provided")
    @Nullable
    private String aadhaarNumber; // Only used for ABHA creation, not stored directly

    @Pattern(regexp = "^$|^[0-9]{10}$", message = "ABHA Link Mobile number must be 10 digits if provided")
    private String abhaLinkMobileNumber; // Mobile for ABHA creation/linking OTP

    private String abhaIdToLink;

    @NotNull(message = "Organization ID is required")
    private Integer organizationId;
}

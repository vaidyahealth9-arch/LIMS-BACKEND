package com.halo.lims.dto.patient;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

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
    private String contactPhone; 

    @Email(message = "Email ID must be a valid email format")
    @Size(max = 100, message = "Email ID must not exceed 100 characters")
    private String contactEmail; 

    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String addressLine1; 

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    private String addressLine2; 

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @Pattern(regexp = "^$|^[0-9]{12}$", message = "Aadhaar number must be 12 digits if provided")
    private String aadhaarNumber; 

    @Pattern(regexp = "^$|^[0-9]{10}$", message = "ABHA Link Mobile number must be 10 digits if provided")
    private String abhaLinkMobileNumber; 

    private String abhaIdToLink;

    @NotNull(message = "Organization ID is required")
    private Integer organizationId;

    private Integer id; 
    private String relationship;
    private Boolean isDependent;

    public PatientRegistrationRequest() {}

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

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

    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }

    public String getAbhaLinkMobileNumber() { return abhaLinkMobileNumber; }
    public void setAbhaLinkMobileNumber(String abhaLinkMobileNumber) { this.abhaLinkMobileNumber = abhaLinkMobileNumber; }

    public String getAbhaIdToLink() { return abhaIdToLink; }
    public void setAbhaIdToLink(String abhaIdToLink) { this.abhaIdToLink = abhaIdToLink; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public Boolean getIsDependent() { return isDependent; }
    public void setIsDependent(Boolean isDependent) { this.isDependent = isDependent; }
}

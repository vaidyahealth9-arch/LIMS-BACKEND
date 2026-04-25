package com.halo.lims.dto.patient;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class PatientRegistrationResponse {
    private Integer id;
    private String localMrnValue;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
    private String abhaId;
    private String abhaAddress;
    private String abdmLinkStatus;
    private OffsetDateTime createdAt;
    private Integer organizationId;
    private String contactPhone;
    private String contactEmail;
    private String addressLine1;
    private String city;
    private String state;
    private String postalCode;
    private String relationship;
    private Boolean isDependent;

    public PatientRegistrationResponse() {}

    public PatientRegistrationResponse(Integer id, String localMrnValue, String firstName, String lastName, String gender, LocalDate dateOfBirth, String abhaId, String abhaAddress, String abdmLinkStatus, OffsetDateTime createdAt, Integer organizationId, String contactPhone, String contactEmail, String addressLine1, String city, String state, String postalCode, String relationship, Boolean isDependent) {
        this.id = id;
        this.localMrnValue = localMrnValue;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.abhaId = abhaId;
        this.abhaAddress = abhaAddress;
        this.abdmLinkStatus = abdmLinkStatus;
        this.createdAt = createdAt;
        this.organizationId = organizationId;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.relationship = relationship;
        this.isDependent = isDependent;
    }

    public static PatientRegistrationResponseBuilder builder() {
        return new PatientRegistrationResponseBuilder();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLocalMrnValue() { return localMrnValue; }
    public void setLocalMrnValue(String localMrnValue) { this.localMrnValue = localMrnValue; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAbhaId() { return abhaId; }
    public void setAbhaId(String abhaId) { this.abhaId = abhaId; }

    public String getAbhaAddress() { return abhaAddress; }
    public void setAbhaAddress(String abhaAddress) { this.abhaAddress = abhaAddress; }

    public String getAbdmLinkStatus() { return abdmLinkStatus; }
    public void setAbdmLinkStatus(String abdmLinkStatus) { this.abdmLinkStatus = abdmLinkStatus; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public Boolean getIsDependent() { return isDependent; }
    public void setIsDependent(Boolean isDependent) { this.isDependent = isDependent; }

    public static class PatientRegistrationResponseBuilder {
        private Integer id;
        private String localMrnValue;
        private String firstName;
        private String lastName;
        private String gender;
        private LocalDate dateOfBirth;
        private String abhaId;
        private String abhaAddress;
        private String abdmLinkStatus;
        private OffsetDateTime createdAt;
        private Integer organizationId;
        private String contactPhone;
        private String contactEmail;
        private String addressLine1;
        private String city;
        private String state;
        private String postalCode;
        private String relationship;
        private Boolean isDependent;

        public PatientRegistrationResponseBuilder id(Integer id) { this.id = id; return this; }
        public PatientRegistrationResponseBuilder localMrnValue(String localMrnValue) { this.localMrnValue = localMrnValue; return this; }
        public PatientRegistrationResponseBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public PatientRegistrationResponseBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public PatientRegistrationResponseBuilder gender(String gender) { this.gender = gender; return this; }
        public PatientRegistrationResponseBuilder dateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; return this; }
        public PatientRegistrationResponseBuilder abhaId(String abhaId) { this.abhaId = abhaId; return this; }
        public PatientRegistrationResponseBuilder abhaAddress(String abhaAddress) { this.abhaAddress = abhaAddress; return this; }
        public PatientRegistrationResponseBuilder abdmLinkStatus(String abdmLinkStatus) { this.abdmLinkStatus = abdmLinkStatus; return this; }
        public PatientRegistrationResponseBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PatientRegistrationResponseBuilder organizationId(Integer organizationId) { this.organizationId = organizationId; return this; }
        public PatientRegistrationResponseBuilder contactPhone(String contactPhone) { this.contactPhone = contactPhone; return this; }
        public PatientRegistrationResponseBuilder contactEmail(String contactEmail) { this.contactEmail = contactEmail; return this; }
        public PatientRegistrationResponseBuilder addressLine1(String addressLine1) { this.addressLine1 = addressLine1; return this; }
        public PatientRegistrationResponseBuilder city(String city) { this.city = city; return this; }
        public PatientRegistrationResponseBuilder state(String state) { this.state = state; return this; }
        public PatientRegistrationResponseBuilder postalCode(String postalCode) { this.postalCode = postalCode; return this; }
        public PatientRegistrationResponseBuilder relationship(String relationship) { this.relationship = relationship; return this; }
        public PatientRegistrationResponseBuilder isDependent(Boolean isDependent) { this.isDependent = isDependent; return this; }

        public PatientRegistrationResponse build() {
            return new PatientRegistrationResponse(id, localMrnValue, firstName, lastName, gender, dateOfBirth, abhaId, abhaAddress, abdmLinkStatus, createdAt, organizationId, contactPhone, contactEmail, addressLine1, city, state, postalCode, relationship, isDependent);
        }
    }
}

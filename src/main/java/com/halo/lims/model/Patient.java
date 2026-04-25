package com.halo.lims.model;

import com.halo.lims.security.JpaConverterJsonCipher;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "patients")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(nullable = false, length = 10)
    private String gender; // "male", "female", "other", "unknown"

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Convert(converter = JpaConverterJsonCipher.class)
    @Column(name = "contact_phone", length = 255)
    private String contactPhone;

    @Convert(converter = JpaConverterJsonCipher.class)
    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Convert(converter = JpaConverterJsonCipher.class)
    @Column(name = "address_line1", length = 512)
    private String addressLine1;

    @Convert(converter = JpaConverterJsonCipher.class)
    @Column(name = "address_line2", length = 512)
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 100)
    private String country; // Default 'IND'

    @Column(name = "abha_address", unique = true, length = 255)
    private String abhaAddress;

    @Column(name = "abha_id", unique = true, length = 255)
    private String abhaId;

    @Column(name = "abha_id_system", length = 255)
    private String abhaIdSystem;

    @Column(name = "abdm_link_status", length = 50)
    private String abdmLinkStatus; // 'NOT_LINKED', 'PENDING_OTP', 'LINKED', 'FAILED'

    @Column(name = "abdm_status_message", columnDefinition = "TEXT")
    private String abdmStatusMessage;

    @Column(name = "abdm_last_linked_at")
    private OffsetDateTime abdmLastLinkedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "local_mrn_system", nullable = false, length = 255)
    private String localMrnSystem;

    @Column(name = "local_mrn_value", unique = true, nullable = false, length = 255)
    private String localMrnValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(length = 50)
    private String relationship; // 'self', 'father', 'mother', 'spouse', 'child', 'other'

    @Column(name = "is_dependent")
    private Boolean isDependent = false;

    @Column(name = "contact_phone_normalized", length = 20)
    private String contactPhoneNormalized;

    @PrePersist
    @PreUpdate
    public void normalizeFields() {
        this.contactPhoneNormalized = normalizePhone(this.contactPhone);
    }

    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String normalized = phone.replaceAll("[^0-9]", "");
        return normalized.isBlank() ? null : normalized;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

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

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAbhaAddress() { return abhaAddress; }
    public void setAbhaAddress(String abhaAddress) { this.abhaAddress = abhaAddress; }

    public String getAbhaId() { return abhaId; }
    public void setAbhaId(String abhaId) { this.abhaId = abhaId; }

    public String getAbhaIdSystem() { return abhaIdSystem; }
    public void setAbhaIdSystem(String abhaIdSystem) { this.abhaIdSystem = abhaIdSystem; }

    public String getAbdmLinkStatus() { return abdmLinkStatus; }
    public void setAbdmLinkStatus(String abdmLinkStatus) { this.abdmLinkStatus = abdmLinkStatus; }

    public String getAbdmStatusMessage() { return abdmStatusMessage; }
    public void setAbdmStatusMessage(String abdmStatusMessage) { this.abdmStatusMessage = abdmStatusMessage; }

    public OffsetDateTime getAbdmLastLinkedAt() { return abdmLastLinkedAt; }
    public void setAbdmLastLinkedAt(OffsetDateTime abdmLastLinkedAt) { this.abdmLastLinkedAt = abdmLastLinkedAt; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public String getLocalMrnSystem() { return localMrnSystem; }
    public void setLocalMrnSystem(String localMrnSystem) { this.localMrnSystem = localMrnSystem; }

    public String getLocalMrnValue() { return localMrnValue; }
    public void setLocalMrnValue(String localMrnValue) { this.localMrnValue = localMrnValue; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public Boolean getIsDependent() { return isDependent; }
    public void setIsDependent(Boolean isDependent) { this.isDependent = isDependent; }

    public String getContactPhoneNormalized() { return contactPhoneNormalized; }
    public void setContactPhoneNormalized(String contactPhoneNormalized) { this.contactPhoneNormalized = contactPhoneNormalized; }
}

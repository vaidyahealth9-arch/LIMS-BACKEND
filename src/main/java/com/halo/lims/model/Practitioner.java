package com.halo.lims.model;


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
@Table(name = "practitioners")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Practitioner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(length = 20)
    private String prefix;

    @Column(length = 10)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "mci_reg_no", length = 100)
    private String mciRegNo;

    @Column(name = "mci_reg_system", length = 255)
    private String mciRegSystem;

    @Column(name = "signature_image", columnDefinition = "TEXT")
    private String signatureImage;

    @Column(name = "signature_image_asset_id")
    private Integer signatureImageAssetId;

    @Column(name = "local_identifier_system", nullable = false, length = 255)
    private String localIdentifierSystem;

    @Column(name = "local_identifier_value", unique = true, nullable = false, length = 255)
    private String localIdentifierValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getMciRegNo() { return mciRegNo; }
    public void setMciRegNo(String mciRegNo) { this.mciRegNo = mciRegNo; }

    public String getMciRegSystem() { return mciRegSystem; }
    public void setMciRegSystem(String mciRegSystem) { this.mciRegSystem = mciRegSystem; }

    public String getSignatureImage() { return signatureImage; }
    public void setSignatureImage(String signatureImage) { this.signatureImage = signatureImage; }

    public Integer getSignatureImageAssetId() { return signatureImageAssetId; }
    public void setSignatureImageAssetId(Integer signatureImageAssetId) { this.signatureImageAssetId = signatureImageAssetId; }

    public String getLocalIdentifierSystem() { return localIdentifierSystem; }
    public void setLocalIdentifierSystem(String localIdentifierSystem) { this.localIdentifierSystem = localIdentifierSystem; }

    public String getLocalIdentifierValue() { return localIdentifierValue; }
    public void setLocalIdentifierValue(String localIdentifierValue) { this.localIdentifierValue = localIdentifierValue; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

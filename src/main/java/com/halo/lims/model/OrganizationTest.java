package com.halo.lims.model;

import com.halo.lims.model.compositeKeys.OrganizationTestId;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "organization_tests")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(OrganizationTestId.class)
public class OrganizationTest {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "price", precision = 10, scale = 2) // Lab-specific price
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specimen_type_id")
    private SpecimenType specimenType;

    @Column(name = "default_number_of_specimens")
    private Integer defaultNumberOfSpecimens;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Getters and Setters
    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public Test getTest() { return test; }
    public void setTest(Test test) { this.test = test; }

    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public SpecimenType getSpecimenType() { return specimenType; }
    public void setSpecimenType(SpecimenType specimenType) { this.specimenType = specimenType; }

    public Integer getDefaultNumberOfSpecimens() { return defaultNumberOfSpecimens; }
    public void setDefaultNumberOfSpecimens(Integer defaultNumberOfSpecimens) { this.defaultNumberOfSpecimens = defaultNumberOfSpecimens; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}


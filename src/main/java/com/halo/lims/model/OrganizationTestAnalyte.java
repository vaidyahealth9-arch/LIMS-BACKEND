package com.halo.lims.model;

import com.halo.lims.model.compositeKeys.OrganizationTestAnalyteId;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "organization_test_analytes")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(OrganizationTestAnalyteId.class)
public class OrganizationTestAnalyte {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_analyte_id", nullable = false)
    private TestAnalyte testAnalyte;

    @Column(name = "result_type", length = 50)
    private String resultType; // e.g., "Numeric", "Text", "Coded"

    @Column(name = "decimal_places")
    private Integer decimalPlaces;

    @Column(name = "biological_ref_interval", columnDefinition = "TEXT")
    private String biologicalRefInterval;

    @Column(name = "price")
    private Double price;

    @Column(name = "code", length = 100)
    private String code;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Getters and Setters
    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public TestAnalyte getTestAnalyte() { return testAnalyte; }
    public void setTestAnalyte(TestAnalyte testAnalyte) { this.testAnalyte = testAnalyte; }

    public String getResultType() { return resultType; }
    public void setResultType(String resultType) { this.resultType = resultType; }

    public Integer getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(Integer decimalPlaces) { this.decimalPlaces = decimalPlaces; }

    public String getBiologicalRefInterval() { return biologicalRefInterval; }
    public void setBiologicalRefInterval(String biologicalRefInterval) { this.biologicalRefInterval = biologicalRefInterval; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

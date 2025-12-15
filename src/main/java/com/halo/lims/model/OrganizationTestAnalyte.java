package com.halo.lims.model;

import com.halo.lims.model.compositeKeys.OrganizationTestAnalyteId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "organization_test_analytes")
@Data
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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

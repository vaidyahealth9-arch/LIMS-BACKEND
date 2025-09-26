package com.halo.lims.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "test_analytes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAnalyte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "analyte_code", unique = true, nullable = false, length = 100)
    private String analyteCode;

    @Column(name = "analyte_name", nullable = false)
    private String analyteName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_test_id", nullable = false)
    private Test parentTest;

    @Column(name = "loinc_code", unique = true, length = 50)
    private String loincCode;

    @Column(name = "loinc_system", length = 255)
    private String loincSystem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(name = "result_type", nullable = false, length = 50)
    private String resultType; // e.g., "Numeric", "Text", "Coded"

    @Column(name = "decimal_places")
    private Integer decimalPlaces;

    @Column(name = "biological_ref_interval", columnDefinition = "TEXT")
    private String biologicalRefInterval;

    @Column(name = "is_derived", nullable = false)
    private Boolean isDerived;

    @Column(columnDefinition = "TEXT")
    private String formula;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

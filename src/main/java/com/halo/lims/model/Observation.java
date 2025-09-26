package com.halo.lims.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "observations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Observation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analyte_id", nullable = false)
    private TestAnalyte analyte; // Links to the specific analyte definition

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specimen_id")
    private Specimen specimen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id", nullable = false)
    private ServiceRequest serviceRequest;

    @Column(name = "effective_date_time", nullable = false)
    private OffsetDateTime effectiveDateTime;

    @Column(name = "issued_date_time", nullable = false)
    private OffsetDateTime issuedDateTime;

    @Column(nullable = false, length = 50)
    private String status; // FHIR ObservationStatus

    @Column(name = "value_numeric", precision = 15, scale = 8) // Adjust precision/scale as needed
    private BigDecimal valueNumeric;

    @Column(name = "value_string", columnDefinition = "TEXT")
    private String valueString;

    @Column(name = "value_code", length = 255)
    private String valueCode;

    @Column(name = "value_code_system", length = 255)
    private String valueCodeSystem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_range_id")
    private ReferenceRange referenceRange;

    @Column(name = "interpretation_code", length = 50)
    private String interpretationCode;

    @Column(name = "interpretation_system", length = 255)
    private String interpretationSystem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_id")
    private Practitioner performer;

    @Column(name = "local_observation_system", nullable = false, length = 255)
    private String localObservationSystem;

    @Column(name = "local_observation_value", unique = true, nullable = false, length = 255)
    private String localObservationValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

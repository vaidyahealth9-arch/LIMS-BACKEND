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
@Table(name = "specimens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Specimen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id", nullable = false)
    private ServiceRequest serviceRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specimen_type_id", nullable = true)
    private SpecimenType specimenType;

    @Column(name = "collection_date", nullable = false)
    private OffsetDateTime collectionDate;

    @Column(name = "received_date")
    private OffsetDateTime receivedDate;

    @Column(nullable = false, length = 50)
    private String status; // FHIR SpecimenStatus

    @Column(name = "container_id", length = 255)
    private String containerId;

    @Column(name = "quantity_value", precision = 10, scale = 4)
    private BigDecimal quantityValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quantity_unit_id")
    private Unit quantityUnit;

    @Column(name = "local_specimen_system", nullable = false, length = 255)
    private String localSpecimenSystem;

    @Column(name = "local_specimen_value", unique = true, nullable = false, length = 255)
    private String localSpecimenValue;

    @Lob
    @Column(name = "barcode", columnDefinition = "TEXT")
    private String barcode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

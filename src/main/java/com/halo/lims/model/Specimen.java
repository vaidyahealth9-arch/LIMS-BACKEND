package com.halo.lims.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "specimens")
@Builder
@AllArgsConstructor
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

    @Builder.Default
    @Column(name = "barcode_regenerated_count", nullable = false)
    private Integer barcodeRegeneratedCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Specimen() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public ServiceRequest getServiceRequest() { return serviceRequest; }
    public void setServiceRequest(ServiceRequest serviceRequest) { this.serviceRequest = serviceRequest; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public SpecimenType getSpecimenType() { return specimenType; }
    public void setSpecimenType(SpecimenType specimenType) { this.specimenType = specimenType; }

    public OffsetDateTime getCollectionDate() { return collectionDate; }
    public void setCollectionDate(OffsetDateTime collectionDate) { this.collectionDate = collectionDate; }

    public OffsetDateTime getReceivedDate() { return receivedDate; }
    public void setReceivedDate(OffsetDateTime receivedDate) { this.receivedDate = receivedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getContainerId() { return containerId; }
    public void setContainerId(String containerId) { this.containerId = containerId; }

    public BigDecimal getQuantityValue() { return quantityValue; }
    public void setQuantityValue(BigDecimal quantityValue) { this.quantityValue = quantityValue; }

    public Unit getQuantityUnit() { return quantityUnit; }
    public void setQuantityUnit(Unit quantityUnit) { this.quantityUnit = quantityUnit; }

    public String getLocalSpecimenSystem() { return localSpecimenSystem; }
    public void setLocalSpecimenSystem(String localSpecimenSystem) { this.localSpecimenSystem = localSpecimenSystem; }

    public String getLocalSpecimenValue() { return localSpecimenValue; }
    public void setLocalSpecimenValue(String localSpecimenValue) { this.localSpecimenValue = localSpecimenValue; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public Integer getBarcodeRegeneratedCount() { return barcodeRegeneratedCount; }
    public void setBarcodeRegeneratedCount(Integer barcodeRegeneratedCount) { this.barcodeRegeneratedCount = barcodeRegeneratedCount; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}

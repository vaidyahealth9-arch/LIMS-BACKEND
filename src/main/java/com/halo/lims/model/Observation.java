package com.halo.lims.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "observations")
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

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

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

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public TestAnalyte getAnalyte() { return analyte; }
    public void setAnalyte(TestAnalyte analyte) { this.analyte = analyte; }

    public Specimen getSpecimen() { return specimen; }
    public void setSpecimen(Specimen specimen) { this.specimen = specimen; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public ServiceRequest getServiceRequest() { return serviceRequest; }
    public void setServiceRequest(ServiceRequest serviceRequest) { this.serviceRequest = serviceRequest; }

    public OffsetDateTime getEffectiveDateTime() { return effectiveDateTime; }
    public void setEffectiveDateTime(OffsetDateTime effectiveDateTime) { this.effectiveDateTime = effectiveDateTime; }

    public OffsetDateTime getIssuedDateTime() { return issuedDateTime; }
    public void setIssuedDateTime(OffsetDateTime issuedDateTime) { this.issuedDateTime = issuedDateTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getValueNumeric() { return valueNumeric; }
    public void setValueNumeric(BigDecimal valueNumeric) { this.valueNumeric = valueNumeric; }

    public String getValueString() { return valueString; }
    public void setValueString(String valueString) { this.valueString = valueString; }

    public String getValueCode() { return valueCode; }
    public void setValueCode(String valueCode) { this.valueCode = valueCode; }

    public String getValueCodeSystem() { return valueCodeSystem; }
    public void setValueCodeSystem(String valueCodeSystem) { this.valueCodeSystem = valueCodeSystem; }

    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }

    public ReferenceRange getReferenceRange() { return referenceRange; }
    public void setReferenceRange(ReferenceRange referenceRange) { this.referenceRange = referenceRange; }

    public String getInterpretationCode() { return interpretationCode; }
    public void setInterpretationCode(String interpretationCode) { this.interpretationCode = interpretationCode; }

    public String getInterpretationSystem() { return interpretationSystem; }
    public void setInterpretationSystem(String interpretationSystem) { this.interpretationSystem = interpretationSystem; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Practitioner getPerformer() { return performer; }
    public void setPerformer(Practitioner performer) { this.performer = performer; }

    public String getLocalObservationSystem() { return localObservationSystem; }
    public void setLocalObservationSystem(String localObservationSystem) { this.localObservationSystem = localObservationSystem; }

    public String getLocalObservationValue() { return localObservationValue; }
    public void setLocalObservationValue(String localObservationValue) { this.localObservationValue = localObservationValue; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

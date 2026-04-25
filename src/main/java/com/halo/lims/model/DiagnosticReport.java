package com.halo.lims.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "diagnostic_reports")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosticReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    @Column(name = "version", columnDefinition = "integer default 0")
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id", nullable = false)
    private ServiceRequest serviceRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id")
    private Encounter encounter;

    @Column(nullable = false, length = 50)
    private String status; // FHIR DiagnosticReportStatus

    @Column(name = "effective_date_time", nullable = false)
    private OffsetDateTime effectiveDateTime;

    @Column(name = "issued_date_time", nullable = false)
    private OffsetDateTime issuedDateTime;

    @Column(name = "report_text", columnDefinition = "TEXT")
    private String reportText;

    @Column(name = "presented_form_url", length = 255)
    private String presentedFormUrl;

    @Column(name = "report_gcs_url", length = 1024)
    private String reportGcsUrl;

    @Column(name = "local_report_system", nullable = false, length = 255)
    private String localReportSystem;

    @Column(name = "local_report_value", unique = true, nullable = false, length = 255)
    private String localReportValue;

    @OneToMany(mappedBy = "diagnosticReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<DiagnosticReportObservation> observations = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public ServiceRequest getServiceRequest() { return serviceRequest; }
    public void setServiceRequest(ServiceRequest serviceRequest) { this.serviceRequest = serviceRequest; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Encounter getEncounter() { return encounter; }
    public void setEncounter(Encounter encounter) { this.encounter = encounter; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getEffectiveDateTime() { return effectiveDateTime; }
    public void setEffectiveDateTime(OffsetDateTime effectiveDateTime) { this.effectiveDateTime = effectiveDateTime; }

    public OffsetDateTime getIssuedDateTime() { return issuedDateTime; }
    public void setIssuedDateTime(OffsetDateTime issuedDateTime) { this.issuedDateTime = issuedDateTime; }

    public String getReportText() { return reportText; }
    public void setReportText(String reportText) { this.reportText = reportText; }

    public String getPresentedFormUrl() { return presentedFormUrl; }
    public void setPresentedFormUrl(String presentedFormUrl) { this.presentedFormUrl = presentedFormUrl; }

    public String getReportGcsUrl() { return reportGcsUrl; }
    public void setReportGcsUrl(String reportGcsUrl) { this.reportGcsUrl = reportGcsUrl; }

    public String getLocalReportSystem() { return localReportSystem; }
    public void setLocalReportSystem(String localReportSystem) { this.localReportSystem = localReportSystem; }

    public String getLocalReportValue() { return localReportValue; }
    public void setLocalReportValue(String localReportValue) { this.localReportValue = localReportValue; }

    public Set<DiagnosticReportObservation> getObservations() { return observations; }
    public void setObservations(Set<DiagnosticReportObservation> observations) { this.observations = observations; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

package com.halo.lims.model;

import jakarta.persistence.*;
import lombok.Data;
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
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosticReport {
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
}

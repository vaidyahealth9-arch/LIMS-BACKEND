package com.halo.lims.model;

import com.halo.lims.model.compositeKeys.DiagnosticReportObservationId;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "diagnostic_report_observations")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(DiagnosticReportObservationId.class)
public class DiagnosticReportObservation {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_report_id")
    private DiagnosticReport diagnosticReport;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "observation_id")
    private Observation observation;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Getters and Setters
    public DiagnosticReport getDiagnosticReport() { return diagnosticReport; }
    public void setDiagnosticReport(DiagnosticReport diagnosticReport) { this.diagnosticReport = diagnosticReport; }

    public Observation getObservation() { return observation; }
    public void setObservation(Observation observation) { this.observation = observation; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

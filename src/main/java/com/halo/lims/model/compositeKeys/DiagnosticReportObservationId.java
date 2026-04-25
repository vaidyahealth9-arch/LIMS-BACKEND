package com.halo.lims.model.compositeKeys;

import com.halo.lims.model.DiagnosticReport;
import com.halo.lims.model.Observation;
import java.io.Serializable;
import java.util.Objects;

public class DiagnosticReportObservationId implements Serializable {
    private DiagnosticReport diagnosticReport;
    private Observation observation;

    public DiagnosticReportObservationId() {}

    public DiagnosticReportObservationId(DiagnosticReport diagnosticReport, Observation observation) {
        this.diagnosticReport = diagnosticReport;
        this.observation = observation;
    }

    // Getters and Setters
    public DiagnosticReport getDiagnosticReport() { return diagnosticReport; }
    public void setDiagnosticReport(DiagnosticReport diagnosticReport) { this.diagnosticReport = diagnosticReport; }

    public Observation getObservation() { return observation; }
    public void setObservation(Observation observation) { this.observation = observation; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiagnosticReportObservationId that = (DiagnosticReportObservationId) o;
        return Objects.equals(diagnosticReport, that.diagnosticReport) && Objects.equals(observation, that.observation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diagnosticReport, observation);
    }
}

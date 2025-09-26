package com.halo.lims.model.compositeKeys;

import com.halo.lims.model.DiagnosticReport;
import com.halo.lims.model.Observation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// Composite Primary Key class for DiagnosticReportObservation
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticReportObservationId implements Serializable {
    private DiagnosticReport diagnosticReport;
    private Observation observation;
}

package com.halo.lims.repository;

import com.halo.lims.model.DiagnosticReport;
import com.halo.lims.model.DiagnosticReportObservation;
import com.halo.lims.model.compositeKeys.DiagnosticReportObservationId;
import com.halo.lims.model.Observation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiagnosticReportObservationRepository extends JpaRepository<DiagnosticReportObservation, DiagnosticReportObservationId> {

    /**
     * Finds all DiagnosticReportObservation links for a specific DiagnosticReport entity.
     * @param diagnosticReport The DiagnosticReport entity.
     * @return A list of DiagnosticReportObservation entities.
     */
    List<DiagnosticReportObservation> findByDiagnosticReport(DiagnosticReport diagnosticReport);

    /**
     * Finds all DiagnosticReportObservation links for a specific DiagnosticReport by its ID.
     * @param diagnosticReportId The ID of the DiagnosticReport.
     * @return A list of DiagnosticReportObservation entities.
     */
    List<DiagnosticReportObservation> findByDiagnosticReport_Id(Integer diagnosticReportId);

    /**
     * Finds a specific DiagnosticReportObservation link by DiagnosticReport ID and Observation ID.
     * @param diagnosticReportId The ID of the DiagnosticReport.
     * @param observationId The ID of the Observation.
     * @return An Optional containing the DiagnosticReportObservation, or empty if not found.
     */
    Optional<DiagnosticReportObservation> findByDiagnosticReport_IdAndObservation_Id(Integer diagnosticReportId, Integer observationId);

    /**
     * Finds all DiagnosticReportObservation links for a specific Observation entity.
     * @param observation The Observation entity.
     * @return A list of DiagnosticReportObservation entities.
     */
    List<DiagnosticReportObservation> findByObservation(Observation observation);

    /**
     * Finds all DiagnosticReportObservation links for a specific Observation by its ID.
     * @param observationId The ID of the Observation.
     * @return A list of DiagnosticReportObservation entities.
     */
    List<DiagnosticReportObservation> findByObservation_Id(Integer observationId);
}

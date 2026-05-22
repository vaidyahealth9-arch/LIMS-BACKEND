package com.halo.lims.repository;

import com.halo.lims.model.Observation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObservationRepository extends JpaRepository<Observation, Integer> {
    List<Observation> findByServiceRequestId(Integer serviceRequestId);

    @Query("""
            SELECT DISTINCT o FROM Observation o
            LEFT JOIN FETCH o.referenceRange
            LEFT JOIN FETCH o.analyte
            LEFT JOIN FETCH o.unit
            LEFT JOIN FETCH o.specimen
            WHERE o.serviceRequest.id = :serviceRequestId
            ORDER BY o.id
            """)
    List<Observation> findByServiceRequestIdWithReferences(@Param("serviceRequestId") Integer serviceRequestId);

    Optional<Observation> findTopByServiceRequestIdOrderByEffectiveDateTimeDesc(Integer serviceRequestId);

    List<Observation> findByPatientInOrderByEffectiveDateTimeDesc(List<com.halo.lims.model.Patient> patients);


    /**
     * Find historical observations for the same analyte, same patient, within the same organization,
     * excluding the current service request. Most recent results first, limited by the caller via Pageable.
     */
    @Query("""
            SELECT o FROM Observation o
            WHERE o.analyte.id = :analyteId
              AND o.patient.id = :patientId
              AND o.patient.organization.id = :organizationId
              AND o.serviceRequest.id <> :excludeServiceRequestId
              AND o.valueNumeric IS NOT NULL
              AND o.status = 'final'
            ORDER BY o.effectiveDateTime DESC
            """)
    List<Observation> findHistoricalByAnalyteAndPatientAndOrganization(
            @Param("analyteId") Integer analyteId,
            @Param("patientId") Integer patientId,
            @Param("organizationId") Integer organizationId,
            @Param("excludeServiceRequestId") Integer excludeServiceRequestId,
            Pageable pageable
    );

    /**
     * Batch query: find historical observations for MULTIPLE analytes at once,
     * same patient, same organization, excluding current service request.
     * Eliminates the N+1 query pattern in smart report generation.
     */
    @Query("""
            SELECT o FROM Observation o
            WHERE o.analyte.id IN :analyteIds
              AND o.patient.id = :patientId
              AND o.patient.organization.id = :organizationId
              AND o.serviceRequest.id <> :excludeServiceRequestId
              AND o.valueNumeric IS NOT NULL
              AND (o.status IN ('final', 'completed', 'amended') OR o.status IS NULL)
            ORDER BY o.analyte.id, o.effectiveDateTime DESC
            """)
    List<Observation> findHistoricalByAnalyteIdsAndPatientAndOrganization(
            @Param("analyteIds") List<Integer> analyteIds,
            @Param("patientId") Integer patientId,
            @Param("organizationId") Integer organizationId,
            @Param("excludeServiceRequestId") Integer excludeServiceRequestId
    );

    @Query("SELECT MAX(o.updatedAt) FROM Observation o WHERE o.serviceRequest.id = :serviceRequestId")
    java.time.OffsetDateTime findMaxUpdatedAtByServiceRequestId(@Param("serviceRequestId") Integer serviceRequestId);
}

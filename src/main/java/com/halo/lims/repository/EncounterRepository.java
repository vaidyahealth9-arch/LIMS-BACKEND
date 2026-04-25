package com.halo.lims.repository;

import com.halo.lims.model.Encounter;
import com.halo.lims.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EncounterRepository extends JpaRepository<Encounter, Integer>, JpaSpecificationExecutor<Encounter> {
    Optional<Encounter> findByLocalEncounterValue(String localEncounterValue);
    List<Encounter> findByPatient(Patient patient);
    List<Encounter> findByPatient_Id(Integer patientId);
    List<Encounter> findByServiceProvider_Id(Integer serviceProviderId);

    Page<Encounter> findAll(Specification<Encounter> spec, Pageable pageable);

        // Find encounters for patients belonging to an organization (paged)
        Page<Encounter> findByPatient_Organization_Id(Integer organizationId, Pageable pageable);

        // Search encounters by organization, optional date range, patient name or MRN
        @org.springframework.data.jpa.repository.Query("SELECT e FROM Encounter e WHERE e.patient.organization.id = :organizationId "
            + "AND (:start IS NULL OR e.startTime >= :start) "
            + "AND (:end IS NULL OR e.startTime <= :end) "
            + "AND (:patientName IS NULL OR LOWER(CONCAT(e.patient.firstName, ' ', e.patient.lastName)) LIKE LOWER(CONCAT('%', :patientName, '%'))) "
            + "AND (:mrnId IS NULL OR e.patient.localMrnValue LIKE CONCAT('%', :mrnId, '%'))")
        org.springframework.data.domain.Page<com.halo.lims.model.Encounter> searchEncounters(
            @org.springframework.data.repository.query.Param("organizationId") Integer organizationId,
            @org.springframework.data.repository.query.Param("start") java.time.OffsetDateTime start,
            @org.springframework.data.repository.query.Param("end") java.time.OffsetDateTime end,
            @org.springframework.data.repository.query.Param("patientName") String patientName,
            @org.springframework.data.repository.query.Param("mrnId") String mrnId,
            Pageable pageable);
}

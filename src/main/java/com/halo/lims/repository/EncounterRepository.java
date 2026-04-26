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

    // Find encounters for patients belonging to an organization (paged)
    Page<Encounter> findByPatient_Organization_Id(Integer organizationId, Pageable pageable);

    // NOTE: searchEncounters is now implemented via JPA Specifications in EncounterService
    // to avoid a Cloud SQL PostgreSQL bytea type-inference bug with nullable LIKE parameters.
    // The findAll(Specification, Pageable) method is inherited from JpaSpecificationExecutor.
}


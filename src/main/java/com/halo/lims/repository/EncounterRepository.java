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
}

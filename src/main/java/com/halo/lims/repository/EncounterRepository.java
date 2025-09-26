package com.halo.lims.repository;

import com.halo.lims.model.Encounter;
import com.halo.lims.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EncounterRepository extends JpaRepository<Encounter, Integer> {
    Optional<Encounter> findByLocalEncounterValue(String localEncounterValue);
    List<Encounter> findByPatient(Patient patient);
    List<Encounter> findByPatient_Id(Integer patientId);
    List<Encounter> findByServiceProvider_Id(Integer serviceProviderId);
}

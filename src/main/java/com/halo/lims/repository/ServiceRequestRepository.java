package com.halo.lims.repository;

import com.halo.lims.model.Encounter;
import com.halo.lims.model.Patient;
import com.halo.lims.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Integer> {
    Optional<ServiceRequest> findByLocalOrderValue(String localOrderValue);
    List<ServiceRequest> findByPatient(Patient patient);
    List<ServiceRequest> findByPatient_Id(Integer patientId);
    List<ServiceRequest> findByRequester_Id(Integer requesterId);
    List<ServiceRequest> findByEncounter_Id(Integer encounterId);
    List<ServiceRequest> findByEncounter(Encounter encounter);
    List<ServiceRequest> findByEncounterIn(List<Encounter> encounters);
}

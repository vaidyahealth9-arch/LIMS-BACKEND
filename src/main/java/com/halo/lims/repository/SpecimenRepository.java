package com.halo.lims.repository;

import com.halo.lims.model.Patient;
import com.halo.lims.model.ServiceRequest;
import com.halo.lims.model.Specimen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecimenRepository extends JpaRepository<Specimen, Integer> {
    Optional<Specimen> findByLocalSpecimenValue(String localSpecimenValue);
    List<Specimen> findByServiceRequest(ServiceRequest serviceRequest);
    List<Specimen> findByServiceRequest_Id(Integer serviceRequestId);
    List<Specimen> findByPatient(Patient patient);
    List<Specimen> findByPatient_Id(Integer patientId);
    List<Specimen> findByContainerId(String containerId);
}

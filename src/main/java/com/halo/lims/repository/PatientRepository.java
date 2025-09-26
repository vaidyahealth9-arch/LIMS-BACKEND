package com.halo.lims.repository;

import com.halo.lims.model.Organization;
import com.halo.lims.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {

    List<Patient> findByOrganization(Organization organization);
    List<Patient> findByOrganizationId(Integer organizationId);

    Optional<Patient> findByOrganizationAndFirstNameContainingIgnoreCaseOrOrganizationAndLastNameContainingIgnoreCase(Organization organization, String query, Organization organization1, String query1);

    Optional<Patient> findByLocalMrnValueAndOrganization(String localMrnValue, Organization organization);
}

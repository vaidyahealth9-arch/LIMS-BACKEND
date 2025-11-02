package com.halo.lims.repository;

import com.halo.lims.model.Organization;
import com.halo.lims.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {

    List<Patient> findByOrganization(Organization organization);
    List<Patient> findByOrganizationId(Integer organizationId);

    long countByOrganizationIdAndCreatedAtBetween(Integer organizationId, OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT p FROM Patient p WHERE p.organization = :organization AND (" +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "p.contactPhone LIKE CONCAT('%', :query, '%') OR " +
           "p.localMrnValue LIKE CONCAT('%', :query, '%') OR " +
           "p.abhaId LIKE CONCAT('%', :query, '%'))")
    Page<Patient> searchPatients(@Param("organization") Organization organization, @Param("query") String query, Pageable pageable);

    Optional<Patient> findByLocalMrnValueAndOrganization(String localMrnValue, Organization organization);
}

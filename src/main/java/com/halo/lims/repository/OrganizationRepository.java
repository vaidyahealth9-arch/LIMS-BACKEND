package com.halo.lims.repository;

import com.halo.lims.model.Organization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
    Optional<Organization> findByLocalIdentifierValue(@NotBlank(message = "Local identifier value is required (e.g., your internal lab code)") @Size(max = 255) String localIdentifierValue);
    List<Organization> findByOrgType(String orgType);
}

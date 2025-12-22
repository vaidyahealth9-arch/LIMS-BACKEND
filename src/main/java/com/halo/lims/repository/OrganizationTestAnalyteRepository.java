package com.halo.lims.repository;

import com.halo.lims.model.OrganizationTestAnalyte;
import com.halo.lims.model.compositeKeys.OrganizationTestAnalyteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationTestAnalyteRepository extends JpaRepository<OrganizationTestAnalyte, OrganizationTestAnalyteId> {
    List<OrganizationTestAnalyte> findByOrganizationId(Integer organizationId);
    Optional<OrganizationTestAnalyte> findByOrganizationIdAndTestAnalyteId(Integer organizationId, Integer testAnalyteId);
}

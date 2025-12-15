package com.halo.lims.repository;

import com.halo.lims.model.OrganizationTestAnalyte;
import com.halo.lims.model.compositeKeys.OrganizationTestAnalyteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationTestAnalyteRepository extends JpaRepository<OrganizationTestAnalyte, OrganizationTestAnalyteId> {
}

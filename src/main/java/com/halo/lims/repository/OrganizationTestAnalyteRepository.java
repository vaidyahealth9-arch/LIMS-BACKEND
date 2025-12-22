package com.halo.lims.repository;

import com.halo.lims.model.OrganizationTestAnalyte;
import com.halo.lims.model.compositeKeys.OrganizationTestAnalyteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface OrganizationTestAnalyteRepository extends JpaRepository<OrganizationTestAnalyte, OrganizationTestAnalyteId> {
    List<OrganizationTestAnalyte> findByOrganizationId(Integer organizationId);
    Optional<OrganizationTestAnalyte> findByOrganizationIdAndTestAnalyteId(Integer organizationId, Integer testAnalyteId);

    @Modifying
    @Query("DELETE FROM OrganizationTestAnalyte ota WHERE ota.organization.id = :organizationId AND ota.testAnalyte.parentTest.id = :testId")
    void deleteByOrganizationIdAndTestId(@Param("organizationId") Integer organizationId, @Param("testId") Integer testId);
}

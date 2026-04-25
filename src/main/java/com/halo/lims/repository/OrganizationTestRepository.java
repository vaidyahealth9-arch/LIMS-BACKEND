package com.halo.lims.repository;

import com.halo.lims.model.Organization;
import com.halo.lims.model.OrganizationTest;
import com.halo.lims.model.compositeKeys.OrganizationTestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationTestRepository extends JpaRepository<OrganizationTest, OrganizationTestId> {

    List<OrganizationTest> findByOrganization(Organization organization);
    List<OrganizationTest> findByOrganization_Id(Integer organizationId);
    List<OrganizationTest> findByOrganization_IdAndIsEnabled(Integer organizationId, Boolean isEnabled);
    Optional<OrganizationTest> findByOrganization_IdAndTest_Id(Integer organizationId, Integer testId);
    List<OrganizationTest> findByOrganization_IdAndTest_IdIn(Integer organizationId, List<Integer> testIds);
    boolean existsByOrganization_IdAndTest_IdAndIsEnabled(Integer organizationId, Integer testId, Boolean isEnabled);

    boolean existsByTest_Id(Integer id);
}

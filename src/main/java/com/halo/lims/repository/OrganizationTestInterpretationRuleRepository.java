package com.halo.lims.repository;

import com.halo.lims.model.Organization;
import com.halo.lims.model.OrganizationTestInterpretationRule;
import com.halo.lims.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationTestInterpretationRuleRepository extends JpaRepository<OrganizationTestInterpretationRule, Integer> {
    List<OrganizationTestInterpretationRule> findByOrganizationTestOrganizationAndOrganizationTestTest(Organization organization, Test test);
}

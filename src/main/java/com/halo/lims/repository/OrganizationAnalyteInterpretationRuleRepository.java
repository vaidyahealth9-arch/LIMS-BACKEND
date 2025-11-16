package com.halo.lims.repository;

import com.halo.lims.model.Organization;
import com.halo.lims.model.OrganizationAnalyteInterpretationRule;
import com.halo.lims.model.TestAnalyte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationAnalyteInterpretationRuleRepository extends JpaRepository<OrganizationAnalyteInterpretationRule, Integer> {
    List<OrganizationAnalyteInterpretationRule> findByOrganizationAndAnalyte(Organization organization, TestAnalyte analyte);
}

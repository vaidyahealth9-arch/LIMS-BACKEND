package com.halo.lims.repository;

import com.halo.lims.model.OrganizationAnalyteInterpretationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationAnalyteInterpretationRuleRepository extends JpaRepository<OrganizationAnalyteInterpretationRule, Integer> {
    OrganizationAnalyteInterpretationRule findByAnalyteIdAndOrganizationId(Integer analyteId, Integer organizationId);
}

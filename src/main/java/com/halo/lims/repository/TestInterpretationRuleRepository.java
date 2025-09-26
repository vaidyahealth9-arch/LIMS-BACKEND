package com.halo.lims.repository;

import com.halo.lims.model.TestAnalyte;
import com.halo.lims.model.TestInterpretationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestInterpretationRuleRepository extends JpaRepository<TestInterpretationRule, Integer> {
    List<TestInterpretationRule> findByAnalyte(TestAnalyte analyte);
    Optional<TestInterpretationRule> findByRuleId(String ruleId);
    List<TestInterpretationRule> findByAnalyteId(Integer analyteId);
    boolean existsByRuleId(String ruleId);
}

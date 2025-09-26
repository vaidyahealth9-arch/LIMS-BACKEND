package com.halo.lims.service;

import com.halo.lims.dto.test.TestInterpretationRuleCreateRequest;
import com.halo.lims.dto.test.TestInterpretationRuleResponse;
import com.halo.lims.dto.test.TestInterpretationRuleUpdateRequest;
import com.halo.lims.model.TestAnalyte;
import com.halo.lims.model.TestInterpretationRule;
import com.halo.lims.repository.TestAnalyteRepository;
import com.halo.lims.repository.TestInterpretationRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestInterpretationRuleService {

    private final TestInterpretationRuleRepository testInterpretationRuleRepository;
    private final TestAnalyteRepository testAnalyteRepository;

    public TestInterpretationRuleService(TestInterpretationRuleRepository testInterpretationRuleRepository, TestAnalyteRepository testAnalyteRepository) {
        this.testInterpretationRuleRepository = testInterpretationRuleRepository;
        this.testAnalyteRepository = testAnalyteRepository;
    }

    @Transactional
    public TestInterpretationRuleResponse createTestInterpretationRule(TestInterpretationRuleCreateRequest request) {
        if (testInterpretationRuleRepository.existsByRuleId(request.getRuleId())) {
            throw new IllegalArgumentException("Test Interpretation Rule with ID " + request.getRuleId() + " already exists.");
        }

        TestAnalyte analyte = testAnalyteRepository.findById(request.getAnalyteId())
                .orElseThrow(() -> new RuntimeException("Test Analyte not found with ID: " + request.getAnalyteId()));

        TestInterpretationRule rule = TestInterpretationRule.builder()
                .ruleId(request.getRuleId())
                .analyte(analyte)
                .conditionExpression(request.getConditionExpression())
                .classification(request.getClassification())
                .autoComment(request.getAutoComment())
                .reflexActionText(request.getReflexActionText())
                .priority(request.getPriority())
                .build();

        TestInterpretationRule savedRule = testInterpretationRuleRepository.save(rule);
        return mapToTestInterpretationRuleResponse(savedRule);
    }

    @Transactional
    public TestInterpretationRuleResponse updateTestInterpretationRule(Integer id, TestInterpretationRuleUpdateRequest request) {
        TestInterpretationRule rule = testInterpretationRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test Interpretation Rule not found with ID: " + id));

        if (request.getClassification() != null) rule.setClassification(request.getClassification());
        if (request.getAutoComment() != null) rule.setAutoComment(request.getAutoComment());
        if (request.getReflexActionText() != null) rule.setReflexActionText(request.getReflexActionText());
        if (request.getPriority() != null) rule.setPriority(request.getPriority());

        TestInterpretationRule updatedRule = testInterpretationRuleRepository.save(rule);
        return mapToTestInterpretationRuleResponse(updatedRule);
    }

    @Transactional(readOnly = true)
    public TestInterpretationRuleResponse getTestInterpretationRuleById(Integer id) {
        TestInterpretationRule rule = testInterpretationRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test Interpretation Rule not found with ID: " + id));
        return mapToTestInterpretationRuleResponse(rule);
    }

    @Transactional(readOnly = true)
    public List<TestInterpretationRuleResponse> getTestInterpretationRulesByAnalyte(Integer analyteId) {
        testAnalyteRepository.findById(analyteId) // Validate analyte exists
                .orElseThrow(() -> new RuntimeException("Test Analyte not found with ID: " + analyteId));
        return testInterpretationRuleRepository.findByAnalyteId(analyteId).stream()
                .map(this::mapToTestInterpretationRuleResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TestInterpretationRuleResponse> getAllTestInterpretationRules() {
        return testInterpretationRuleRepository.findAll().stream()
                .map(this::mapToTestInterpretationRuleResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTestInterpretationRule(Integer id) {
        if (!testInterpretationRuleRepository.existsById(id)) {
            throw new RuntimeException("Test Interpretation Rule not found with ID: " + id);
        }
        testInterpretationRuleRepository.deleteById(id);
    }

    private TestInterpretationRuleResponse mapToTestInterpretationRuleResponse(TestInterpretationRule rule) {
        TestInterpretationRuleResponse response = new TestInterpretationRuleResponse();
        response.setId(rule.getId());
        response.setRuleId(rule.getRuleId());
        response.setAnalyteId(rule.getAnalyte().getId());
        response.setAnalyteCode(rule.getAnalyte().getAnalyteCode());
        response.setAnalyteName(rule.getAnalyte().getAnalyteName());
        response.setConditionExpression(rule.getConditionExpression());
        response.setClassification(rule.getClassification());
        response.setAutoComment(rule.getAutoComment());
        response.setReflexActionText(rule.getReflexActionText());
        response.setPriority(rule.getPriority());
        response.setCreatedAt(rule.getCreatedAt());
        response.setUpdatedAt(rule.getUpdatedAt());
        return response;
    }
}

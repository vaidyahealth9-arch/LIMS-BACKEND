package com.halo.lims.service;

import com.halo.lims.dto.test.OrganizationTestInterpretationRuleCreateRequest;
import com.halo.lims.dto.test.OrganizationTestInterpretationRuleResponse;
import com.halo.lims.dto.test.OrganizationTestInterpretationRuleUpdateRequest;
import com.halo.lims.model.OrganizationTest;
import com.halo.lims.model.OrganizationTestInterpretationRule;
import com.halo.lims.model.compositeKeys.OrganizationTestId;
import com.halo.lims.repository.OrganizationTestInterpretationRuleRepository;
import com.halo.lims.repository.OrganizationTestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizationTestInterpretationRuleService {

    private final OrganizationTestInterpretationRuleRepository organizationTestInterpretationRuleRepository;
    private final OrganizationTestRepository organizationTestRepository;

    public OrganizationTestInterpretationRuleService(OrganizationTestInterpretationRuleRepository organizationTestInterpretationRuleRepository, OrganizationTestRepository organizationTestRepository) {
        this.organizationTestInterpretationRuleRepository = organizationTestInterpretationRuleRepository;
        this.organizationTestRepository = organizationTestRepository;
    }

    @Transactional
    public OrganizationTestInterpretationRuleResponse createOrganizationTestInterpretationRule(OrganizationTestInterpretationRuleCreateRequest request) {
        OrganizationTest organizationTest = organizationTestRepository.findById(request.getOrganizationTestId())
                .orElseThrow(() -> new RuntimeException("Organization Test not found with ID: " + request.getOrganizationTestId()));

        OrganizationTestInterpretationRule rule = OrganizationTestInterpretationRule.builder()
                .organizationTest(organizationTest)
                .conditionExpression(request.getConditionExpression())
                .classification(request.getClassification())
                .autoComment(request.getAutoComment())
                .reflexActionText(request.getReflexActionText())
                .priority(request.getPriority())
                .build();

        OrganizationTestInterpretationRule savedRule = organizationTestInterpretationRuleRepository.save(rule);
        return mapToOrganizationTestInterpretationRuleResponse(savedRule);
    }

    @Transactional
    public OrganizationTestInterpretationRuleResponse updateOrganizationTestInterpretationRule(Integer id, OrganizationTestInterpretationRuleUpdateRequest request) {
        OrganizationTestInterpretationRule rule = organizationTestInterpretationRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization Test Interpretation Rule not found with ID: " + id));

        if (request.getClassification() != null) rule.setClassification(request.getClassification());
        if (request.getAutoComment() != null) rule.setAutoComment(request.getAutoComment());
        if (request.getReflexActionText() != null) rule.setReflexActionText(request.getReflexActionText());
        if (request.getPriority() != null) rule.setPriority(request.getPriority());

        OrganizationTestInterpretationRule updatedRule = organizationTestInterpretationRuleRepository.save(rule);
        return mapToOrganizationTestInterpretationRuleResponse(updatedRule);
    }

    @Transactional(readOnly = true)
    public OrganizationTestInterpretationRuleResponse getOrganizationTestInterpretationRuleById(Integer id) {
        OrganizationTestInterpretationRule rule = organizationTestInterpretationRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization Test Interpretation Rule not found with ID: " + id));
        return mapToOrganizationTestInterpretationRuleResponse(rule);
    }

    @Transactional(readOnly = true)
    public List<OrganizationTestInterpretationRuleResponse> getOrganizationTestInterpretationRulesByOrganizationTest(OrganizationTestId organizationTestId) {
        organizationTestRepository.findById(organizationTestId) // Validate organization test exists
                .orElseThrow(() -> new RuntimeException("Organization Test not found with ID: " + organizationTestId));
        return organizationTestInterpretationRuleRepository.findByOrganizationTestOrganizationAndOrganizationTestTest(organizationTestId.getOrganization(), organizationTestId.getTest()).stream()
                .map(this::mapToOrganizationTestInterpretationRuleResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrganizationTestInterpretationRuleResponse> getAllOrganizationTestInterpretationRules() {
        return organizationTestInterpretationRuleRepository.findAll().stream()
                .map(this::mapToOrganizationTestInterpretationRuleResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteOrganizationTestInterpretationRule(Integer id) {
        if (!organizationTestInterpretationRuleRepository.existsById(id)) {
            throw new RuntimeException("Organization Test Interpretation Rule not found with ID: " + id);
        }
        organizationTestInterpretationRuleRepository.deleteById(id);
    }

    private OrganizationTestInterpretationRuleResponse mapToOrganizationTestInterpretationRuleResponse(OrganizationTestInterpretationRule rule) {
        OrganizationTestInterpretationRuleResponse response = new OrganizationTestInterpretationRuleResponse();
        response.setId(rule.getId());
        response.setOrganizationTestId(new OrganizationTestId(rule.getOrganizationTest().getOrganization(), rule.getOrganizationTest().getTest()));
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

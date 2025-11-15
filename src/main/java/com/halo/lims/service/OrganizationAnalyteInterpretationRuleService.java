package com.halo.lims.service;

import com.halo.lims.dto.test.InterpretationRuleResponse;
import com.halo.lims.dto.test.OrganizationAnalyteInterpretationRuleCreateRequest;
import com.halo.lims.dto.test.OrganizationAnalyteInterpretationRuleResponse;
import com.halo.lims.dto.test.OrganizationAnalyteInterpretationRuleUpdateRequest;
import com.halo.lims.model.Organization;
import com.halo.lims.model.OrganizationAnalyteInterpretationRule;
import com.halo.lims.model.TestAnalyte;
import com.halo.lims.repository.OrganizationAnalyteInterpretationRuleRepository;
import com.halo.lims.repository.OrganizationRepository;
import com.halo.lims.repository.TestAnalyteRepository;
import com.halo.lims.repository.TestInterpretationRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizationAnalyteInterpretationRuleService {

    private final OrganizationAnalyteInterpretationRuleRepository organizationAnalyteInterpretationRuleRepository;
    private final OrganizationRepository organizationRepository;
    private final TestAnalyteRepository testAnalyteRepository;
    private final TestInterpretationRuleRepository testInterpretationRuleRepository;

    public OrganizationAnalyteInterpretationRuleService(OrganizationAnalyteInterpretationRuleRepository organizationAnalyteInterpretationRuleRepository,
                                                      OrganizationRepository organizationRepository,
                                                      TestAnalyteRepository testAnalyteRepository,
                                                      TestInterpretationRuleRepository testInterpretationRuleRepository) {
        this.organizationAnalyteInterpretationRuleRepository = organizationAnalyteInterpretationRuleRepository;
        this.organizationRepository = organizationRepository;
        this.testAnalyteRepository = testAnalyteRepository;
        this.testInterpretationRuleRepository = testInterpretationRuleRepository;
    }

    @Transactional
    public OrganizationAnalyteInterpretationRuleResponse createOrganizationAnalyteInterpretationRule(OrganizationAnalyteInterpretationRuleCreateRequest request) {
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + request.getOrganizationId()));
        TestAnalyte analyte = testAnalyteRepository.findById(request.getAnalyteId())
                .orElseThrow(() -> new RuntimeException("Test Analyte not found with ID: " + request.getAnalyteId()));

        OrganizationAnalyteInterpretationRule rule = OrganizationAnalyteInterpretationRule.builder()
                .organization(organization)
                .analyte(analyte)
                .conditionExpression(request.getConditionExpression())
                .classification(request.getClassification())
                .autoComment(request.getAutoComment())
                .reflexActionText(request.getReflexActionText())
                .priority(request.getPriority())
                .build();

        OrganizationAnalyteInterpretationRule savedRule = organizationAnalyteInterpretationRuleRepository.save(rule);
        return mapToOrganizationAnalyteInterpretationRuleResponse(savedRule);
    }

    @Transactional
    public OrganizationAnalyteInterpretationRuleResponse updateOrganizationAnalyteInterpretationRule(Integer id, OrganizationAnalyteInterpretationRuleUpdateRequest request) {
        OrganizationAnalyteInterpretationRule rule = organizationAnalyteInterpretationRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization Analyte Interpretation Rule not found with ID: " + id));

        if (request.getClassification() != null) rule.setClassification(request.getClassification());
        if (request.getAutoComment() != null) rule.setAutoComment(request.getAutoComment());
        if (request.getReflexActionText() != null) rule.setReflexActionText(request.getReflexActionText());
        if (request.getPriority() != null) rule.setPriority(request.getPriority());

        OrganizationAnalyteInterpretationRule updatedRule = organizationAnalyteInterpretationRuleRepository.save(rule);
        return mapToOrganizationAnalyteInterpretationRuleResponse(updatedRule);
    }

    @Transactional(readOnly = true)
    public OrganizationAnalyteInterpretationRuleResponse getOrganizationAnalyteInterpretationRuleById(Integer id) {
        OrganizationAnalyteInterpretationRule rule = organizationAnalyteInterpretationRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization Analyte Interpretation Rule not found with ID: " + id));
        return mapToOrganizationAnalyteInterpretationRuleResponse(rule);
    }

    @Transactional(readOnly = true)
    public List<OrganizationAnalyteInterpretationRuleResponse> getAllOrganizationAnalyteInterpretationRules() {
        return organizationAnalyteInterpretationRuleRepository.findAll().stream()
                .map(this::mapToOrganizationAnalyteInterpretationRuleResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteOrganizationAnalyteInterpretationRule(Integer id) {
        if (!organizationAnalyteInterpretationRuleRepository.existsById(id)) {
            throw new RuntimeException("Organization Analyte Interpretation Rule not found with ID: " + id);
        }
        organizationAnalyteInterpretationRuleRepository.deleteById(id);
    }

    private OrganizationAnalyteInterpretationRuleResponse mapToOrganizationAnalyteInterpretationRuleResponse(OrganizationAnalyteInterpretationRule rule) {
        OrganizationAnalyteInterpretationRuleResponse response = new OrganizationAnalyteInterpretationRuleResponse();
        response.setId(rule.getId());
        response.setOrganizationId(rule.getOrganization().getId());
        response.setAnalyteId(rule.getAnalyte().getId());
        response.setConditionExpression(rule.getConditionExpression());
        response.setClassification(rule.getClassification());
        response.setAutoComment(rule.getAutoComment());
        response.setReflexActionText(rule.getReflexActionText());
        response.setPriority(rule.getPriority());
        response.setCreatedAt(rule.getCreatedAt());
        response.setUpdatedAt(rule.getUpdatedAt());
        return response;
    }

    @Transactional(readOnly = true)
    public List<InterpretationRuleResponse> getInterpretationRules(Integer organizationId, Integer testId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));
        List<TestAnalyte> analytes = testAnalyteRepository.findByParentTestId(testId);

        return analytes.stream().map(analyte -> {
            List<OrganizationAnalyteInterpretationRule> orgRules = organizationAnalyteInterpretationRuleRepository.findByOrganizationAndAnalyte(organization, analyte);
            if (!orgRules.isEmpty()) {
                return orgRules.stream().map(rule -> mapToInterpretationRuleResponse(rule)).collect(Collectors.toList());
            }

            List<com.halo.lims.model.TestInterpretationRule> globalRules = testInterpretationRuleRepository.findByAnalyte(analyte);
            if (!globalRules.isEmpty()) {
                return globalRules.stream().map(rule -> mapToInterpretationRuleResponse(rule)).collect(Collectors.toList());
            }

            return List.of(mapToInterpretationRuleResponse(analyte));
        }).flatMap(List::stream).collect(Collectors.toList());
    }

    private InterpretationRuleResponse mapToInterpretationRuleResponse(Object rule) {
        if (rule instanceof OrganizationAnalyteInterpretationRule) {
            OrganizationAnalyteInterpretationRule orgRule = (OrganizationAnalyteInterpretationRule) rule;
            return InterpretationRuleResponse.builder()
                    .id(orgRule.getId())
                    .organizationId(orgRule.getOrganization().getId())
                    .analyteId(orgRule.getAnalyte().getId())
                    .analyteName(orgRule.getAnalyte().getAnalyteName())
                    .conditionExpression(orgRule.getConditionExpression())
                    .classification(orgRule.getClassification())
                    .autoComment(orgRule.getAutoComment())
                    .reflexActionText(orgRule.getReflexActionText())
                    .priority(orgRule.getPriority())
                    .createdAt(orgRule.getCreatedAt())
                    .updatedAt(orgRule.getUpdatedAt())
                    .ruleSource("Organization")
                    .build();
        } else if (rule instanceof com.halo.lims.model.TestInterpretationRule) {
            com.halo.lims.model.TestInterpretationRule globalRule = (com.halo.lims.model.TestInterpretationRule) rule;
            return InterpretationRuleResponse.builder()
                    .id(globalRule.getId())
                    .analyteId(globalRule.getAnalyte().getId())
                    .analyteName(globalRule.getAnalyte().getAnalyteName())
                    .conditionExpression(globalRule.getConditionExpression())
                    .classification(globalRule.getClassification())
                    .autoComment(globalRule.getAutoComment())
                    .reflexActionText(globalRule.getReflexActionText())
                    .priority(globalRule.getPriority())
                    .createdAt(globalRule.getCreatedAt())
                    .updatedAt(globalRule.getUpdatedAt())
                    .ruleSource("Global")
                    .build();
        } else if (rule instanceof TestAnalyte) {
            TestAnalyte analyte = (TestAnalyte) rule;
            return InterpretationRuleResponse.builder()
                    .analyteId(analyte.getId())
                    .analyteName(analyte.getAnalyteName())
                    .build();
        }
        return null;
    }
}

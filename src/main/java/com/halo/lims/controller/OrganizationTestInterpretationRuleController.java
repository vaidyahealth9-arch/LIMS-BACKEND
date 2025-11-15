package com.halo.lims.controller;

import com.halo.lims.dto.test.OrganizationTestInterpretationRuleCreateRequest;
import com.halo.lims.dto.test.OrganizationTestInterpretationRuleResponse;
import com.halo.lims.dto.test.OrganizationTestInterpretationRuleUpdateRequest;
import com.halo.lims.model.Organization;
import com.halo.lims.model.Test;
import com.halo.lims.model.compositeKeys.OrganizationTestId;
import com.halo.lims.repository.OrganizationRepository;
import com.halo.lims.repository.TestRepository;
import com.halo.lims.service.OrganizationTestInterpretationRuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/organization-test-interpretation-rules")
public class OrganizationTestInterpretationRuleController {

    private final OrganizationTestInterpretationRuleService organizationTestInterpretationRuleService;
    private final OrganizationRepository organizationRepository;
    private final TestRepository testRepository;

    public OrganizationTestInterpretationRuleController(OrganizationTestInterpretationRuleService organizationTestInterpretationRuleService,
                                                    OrganizationRepository organizationRepository,
                                                    TestRepository testRepository) {
        this.organizationTestInterpretationRuleService = organizationTestInterpretationRuleService;
        this.organizationRepository = organizationRepository;
        this.testRepository = testRepository;
    }

    @PostMapping
    public ResponseEntity<OrganizationTestInterpretationRuleResponse> createOrganizationTestInterpretationRule(@RequestBody OrganizationTestInterpretationRuleCreateRequest request) {
        OrganizationTestInterpretationRuleResponse response = organizationTestInterpretationRuleService.createOrganizationTestInterpretationRule(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrganizationTestInterpretationRuleResponse>> getOrganizationTestInterpretationRulesByOrganizationTest(@RequestParam Integer organizationId, @RequestParam Integer testId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found with ID: " + testId));
        OrganizationTestId organizationTestId = new OrganizationTestId(organization, test);
        List<OrganizationTestInterpretationRuleResponse> response = organizationTestInterpretationRuleService.getOrganizationTestInterpretationRulesByOrganizationTest(organizationTestId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationTestInterpretationRuleResponse> updateOrganizationTestInterpretationRule(@PathVariable Integer id, @RequestBody OrganizationTestInterpretationRuleUpdateRequest request) {
        OrganizationTestInterpretationRuleResponse response = organizationTestInterpretationRuleService.updateOrganizationTestInterpretationRule(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

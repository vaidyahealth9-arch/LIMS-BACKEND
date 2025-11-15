package com.halo.lims.controller;

import com.halo.lims.dto.test.InterpretationRuleResponse;
import com.halo.lims.dto.test.OrganizationAnalyteInterpretationRuleCreateRequest;
import com.halo.lims.dto.test.OrganizationAnalyteInterpretationRuleResponse;
import com.halo.lims.dto.test.OrganizationAnalyteInterpretationRuleUpdateRequest;
import com.halo.lims.service.OrganizationAnalyteInterpretationRuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/interpretation-rules")
public class OrganizationAnalyteInterpretationRuleController {

    private final OrganizationAnalyteInterpretationRuleService organizationAnalyteInterpretationRuleService;

    public OrganizationAnalyteInterpretationRuleController(OrganizationAnalyteInterpretationRuleService organizationAnalyteInterpretationRuleService) {
        this.organizationAnalyteInterpretationRuleService = organizationAnalyteInterpretationRuleService;
    }

    @GetMapping
    public ResponseEntity<List<InterpretationRuleResponse>> getInterpretationRules(@RequestParam Integer organizationId, @RequestParam Integer testId) {
        List<InterpretationRuleResponse> response = organizationAnalyteInterpretationRuleService.getInterpretationRules(organizationId, testId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<OrganizationAnalyteInterpretationRuleResponse> createOrganizationAnalyteInterpretationRule(@RequestBody OrganizationAnalyteInterpretationRuleCreateRequest request) {
        OrganizationAnalyteInterpretationRuleResponse response = organizationAnalyteInterpretationRuleService.createOrganizationAnalyteInterpretationRule(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationAnalyteInterpretationRuleResponse> updateOrganizationAnalyteInterpretationRule(@PathVariable Integer id, @RequestBody OrganizationAnalyteInterpretationRuleUpdateRequest request) {
        OrganizationAnalyteInterpretationRuleResponse response = organizationAnalyteInterpretationRuleService.updateOrganizationAnalyteInterpretationRule(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganizationAnalyteInterpretationRule(@PathVariable Integer id) {
        organizationAnalyteInterpretationRuleService.deleteOrganizationAnalyteInterpretationRule(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

package com.halo.lims.controller;

import com.halo.lims.dto.test.TestInterpretationRuleCreateRequest;
import com.halo.lims.dto.test.TestInterpretationRuleResponse;
import com.halo.lims.dto.test.TestInterpretationRuleUpdateRequest;
import com.halo.lims.service.TestInterpretationRuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-interpretation-rules")
public class TestInterpretationRuleController {

    private final TestInterpretationRuleService testInterpretationRuleService;

    public TestInterpretationRuleController(TestInterpretationRuleService testInterpretationRuleService) {
        this.testInterpretationRuleService = testInterpretationRuleService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Only Admin can define global interpretation rules
    public ResponseEntity<TestInterpretationRuleResponse> createTestInterpretationRule(@Valid @RequestBody TestInterpretationRuleCreateRequest request) {
        TestInterpretationRuleResponse response = testInterpretationRuleService.createTestInterpretationRule(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only Admin can update global interpretation rules
    public ResponseEntity<TestInterpretationRuleResponse> updateTestInterpretationRule(@PathVariable Integer id, @Valid @RequestBody TestInterpretationRuleUpdateRequest request) {
        TestInterpretationRuleResponse response = testInterpretationRuleService.updateTestInterpretationRule(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'DOCTOR')") // Various roles can view
    public ResponseEntity<TestInterpretationRuleResponse> getTestInterpretationRuleById(@PathVariable Integer id) {
        TestInterpretationRuleResponse response = testInterpretationRuleService.getTestInterpretationRuleById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'DOCTOR')")
    public ResponseEntity<List<TestInterpretationRuleResponse>> getAllTestInterpretationRules() {
        List<TestInterpretationRuleResponse> responses = testInterpretationRuleService.getAllTestInterpretationRules();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/by-analyte/{analyteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'DOCTOR')")
    public ResponseEntity<List<TestInterpretationRuleResponse>> getTestInterpretationRulesByAnalyte(@PathVariable Integer analyteId) {
        List<TestInterpretationRuleResponse> responses = testInterpretationRuleService.getTestInterpretationRulesByAnalyte(analyteId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only Admin can delete global interpretation rules
    public ResponseEntity<Void> deleteTestInterpretationRule(@PathVariable Integer id) {
        testInterpretationRuleService.deleteTestInterpretationRule(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

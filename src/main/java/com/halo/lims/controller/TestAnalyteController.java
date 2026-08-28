package com.halo.lims.controller;

import com.halo.lims.dto.test.TestAnalyteCreateRequest;
import com.halo.lims.dto.test.TestAnalyteResponse;
import com.halo.lims.dto.test.TestAnalyteUpdateRequest;
import com.halo.lims.service.TestAnalyteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.halo.lims.security.SecurityService;
import com.halo.lims.model.User;

@RestController
@RequestMapping("/api/test-analytes")
public class TestAnalyteController {

    private final TestAnalyteService testAnalyteService;
    private final SecurityService securityService;

    public TestAnalyteController(TestAnalyteService testAnalyteService, SecurityService securityService) {
        this.testAnalyteService = testAnalyteService;
        this.securityService = securityService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR')") // Admin for global, Lab roles for private
    public ResponseEntity<TestAnalyteResponse> createTestAnalyte(@Valid @RequestBody TestAnalyteCreateRequest request) {
        User user = securityService.getAuthenticatedUser();
        if (!user.getRoles().contains("ADMIN")) {
            request.setOrganizationId(user.getOrganization().getId());
        }
        TestAnalyteResponse response = testAnalyteService.createTestAnalyte(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR')")
    public ResponseEntity<TestAnalyteResponse> updateTestAnalyte(@PathVariable Integer id, @Valid @RequestBody TestAnalyteUpdateRequest request) {
        // TODO: TestAnalyteService.updateTestAnalyte needs to verify organization ownership if user is not ADMIN
        TestAnalyteResponse response = testAnalyteService.updateTestAnalyte(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR')") // Various roles can view
    public ResponseEntity<TestAnalyteResponse> getTestAnalyteById(@PathVariable Integer id) {
        TestAnalyteResponse response = testAnalyteService.getTestAnalyteById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR')")
    public ResponseEntity<List<TestAnalyteResponse>> getAllTestAnalytes() {
        List<TestAnalyteResponse> responses = testAnalyteService.getAllTestAnalytes();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/by-parent-test/{parentTestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR')")
    public ResponseEntity<List<TestAnalyteResponse>> getTestAnalytesByParentTest(@PathVariable Integer parentTestId) {
        List<TestAnalyteResponse> responses = testAnalyteService.getTestAnalytesByParentTest(parentTestId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only Admin can delete global test analytes
    public ResponseEntity<Void> deleteTestAnalyte(@PathVariable Integer id) {
        testAnalyteService.deleteTestAnalyte(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

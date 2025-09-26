package com.halo.lims.controller;

import com.halo.lims.dto.organization.test.OrganizationTestRequest;
import com.halo.lims.dto.organization.test.OrganizationTestResponse;
import com.halo.lims.service.OrganizationTestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations/{organizationId}/tests")
public class OrganizationTestController {

    private final OrganizationTestService organizationTestService;

    public OrganizationTestController(OrganizationTestService organizationTestService) {
        this.organizationTestService = organizationTestService;
    }

    /**
     * Adds a test to an organization's catalog or updates an existing entry (enablement, price).
     * Accessible by ADMIN or MANAGER roles for their respective organization.
     * @param organizationId The ID of the organization.
     * @param request The DTO with test details.
     * @return The created/updated OrganizationTestResponse.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') and @securityService.isUserInOrganization(#organizationId)")
    public ResponseEntity<OrganizationTestResponse> addOrUpdateOrganizationTest(
            @PathVariable Integer organizationId,
            @Valid @RequestBody OrganizationTestRequest request) {
        OrganizationTestResponse response = organizationTestService.addOrUpdateOrganizationTest(organizationId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves a specific test entry from an organization's catalog.
     * Accessible by various roles within the organization, and ADMIN.
     * @param organizationId The ID of the organization.
     * @param testId The ID of the test.
     * @return The OrganizationTestResponse.
     */
    @GetMapping("/{testId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN') and @securityService.isUserInOrganization(#organizationId)")
    public ResponseEntity<OrganizationTestResponse> getOrganizationTest(
            @PathVariable Integer organizationId,
            @PathVariable Integer testId) {
        OrganizationTestResponse response = organizationTestService.getOrganizationTest(organizationId, testId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves all test entries (enabled or disabled) for a specific organization.
     * Accessible by ADMIN or MANAGER roles for their respective organization.
     * @param organizationId The ID of the organization.
     * @return A list of OrganizationTestResponses.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') and @securityService.isUserInOrganization(#organizationId)")
    public ResponseEntity<List<OrganizationTestResponse>> getAllOrganizationTests(@PathVariable Integer organizationId) {
        List<OrganizationTestResponse> responses = organizationTestService.getAllOrganizationTests(organizationId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Retrieves only the ENABLED tests for a specific organization (the actual catalog for ordering).
     * Accessible by various roles within the organization, and ADMIN.
     * @param organizationId The ID of the organization.
     * @return A list of OrganizationTestResponses for enabled tests.
     */
    @GetMapping("/enabled")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN', 'DOCTOR') and @securityService.isUserInOrganization(#organizationId)")
    public ResponseEntity<List<OrganizationTestResponse>> getEnabledOrganizationTests(@PathVariable Integer organizationId) {
        List<OrganizationTestResponse> responses = organizationTestService.getEnabledOrganizationTests(organizationId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

}

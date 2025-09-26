package com.halo.lims.controller;

import com.halo.lims.dto.organization.OrganizationCreateRequest;
import com.halo.lims.dto.organization.OrganizationResponse;
import com.halo.lims.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * Creates a new organization (lab, hospital, etc.).
     * Only accessible by ADMIN role.
     * @param request The DTO containing organization details.
     * @return The created OrganizationResponse.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponse> createOrganization(@Valid @RequestBody OrganizationCreateRequest request) {
        OrganizationResponse response = organizationService.createOrganization(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves an organization by its ID.
     * Accessible by ADMIN, MANAGER.
     * @param id The ID of the organization.
     * @return The OrganizationResponse.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and @securityService.isUserInOrganization(#id))")
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable Integer id) {
        OrganizationResponse response = organizationService.getOrganizationById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves all organizations.
     * Only accessible by ADMIN role.
     * @return A list of OrganizationResponses.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        List<OrganizationResponse> responses = organizationService.getAllOrganizations();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    // TODO: Add PUT/PATCH for updating organization details, and DELETE if allowed.
}

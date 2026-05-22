package com.halo.lims.controller;

import com.halo.lims.dto.specimen.SpecimenCreateRequest;
import com.halo.lims.dto.specimen.SpecimenResponse;
import com.halo.lims.dto.specimen.SpecimenUpdateRequest;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.SpecimenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specimens")
public class SpecimenController {

    private final SpecimenService specimenService;
    private final SecurityService securityService;

    public SpecimenController(SpecimenService specimenService, SecurityService securityService) {
        this.specimenService = specimenService;
        this.securityService = securityService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'TECHNICIAN') and @securityService.canAccessServiceRequest(#request.serviceRequestId)")
    public ResponseEntity<SpecimenResponse> createSpecimen(@Valid @RequestBody SpecimenCreateRequest request) {
        SpecimenResponse response = specimenService.createSpecimenResponse(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'TECHNICIAN') and @securityService.canAccessSpecimen(#id)") // Need canAccessSpecimen in SecurityService
    public ResponseEntity<SpecimenResponse> updateSpecimen(@PathVariable Integer id, @Valid @RequestBody SpecimenUpdateRequest request) {
        SpecimenResponse response = specimenService.updateSpecimen(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'TECHNICIAN', 'DOCTOR') and @securityService.canAccessSpecimen(#id)")
    public ResponseEntity<SpecimenResponse> getSpecimenById(@PathVariable Integer id) {
        SpecimenResponse response = specimenService.getSpecimenById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/by-service-request/{serviceRequestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'TECHNICIAN', 'DOCTOR') and @securityService.canAccessServiceRequest(#serviceRequestId)")
    public ResponseEntity<List<SpecimenResponse>> getSpecimensByServiceRequest(@PathVariable Integer serviceRequestId) {
        List<SpecimenResponse> responses = specimenService.getSpecimensByServiceRequest(serviceRequestId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    // Need to add canAccessSpecimen(Integer specimenId) to SecurityService:
    /*
    // In SecurityService.java
    public boolean canAccessSpecimen(Integer specimenId) {
        Specimen specimen = specimenRepository.findById(specimenId).orElse(null);
        if (specimen == null) return false;
        return isUserInOrganization(specimen.getPatient().getOrganization().getId());
    }
    */
}

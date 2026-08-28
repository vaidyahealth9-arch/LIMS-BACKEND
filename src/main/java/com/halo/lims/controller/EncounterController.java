package com.halo.lims.controller;

import com.halo.lims.dto.PagedResponse;
import com.halo.lims.dto.encounter.EncounterCreateRequest;
import com.halo.lims.dto.encounter.EncounterDetailResponse;
import com.halo.lims.dto.encounter.EncounterListResponse;
import com.halo.lims.dto.encounter.EncounterResponse;
import com.halo.lims.dto.encounter.EncounterUpdateRequest;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.EncounterService;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/encounters")
public class EncounterController {

    private static final Logger log = LoggerFactory.getLogger(EncounterController.class);
    
    private final EncounterService encounterService;
    private final SecurityService securityService; // Needed for @PreAuthorize checks

    public EncounterController(EncounterService encounterService, SecurityService securityService) {
        this.encounterService = encounterService;
        this.securityService = securityService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'RECEPTIONIST') and @securityService.canAccessPatient(#request.patientId)")
    public ResponseEntity<EncounterResponse> createEncounter(@Valid @RequestBody EncounterCreateRequest request) {
        EncounterResponse response = encounterService.createEncounter(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'RECEPTIONIST') and @securityService.canAccessEncounter(#id)")
    public ResponseEntity<EncounterResponse> updateEncounter(@PathVariable Integer id, @Valid @RequestBody EncounterUpdateRequest request) {
        EncounterResponse response = encounterService.updateEncounter(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}/workflow-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'RECEPTIONIST', 'DOCTOR', 'PATHOLOGIST') and @securityService.canAccessEncounter(#id)")
    public ResponseEntity<EncounterResponse> updateWorkflowStatus(
            @PathVariable Integer id,
            @RequestParam("status") String status) {
        com.halo.lims.model.User user = securityService.getAuthenticatedUser();
        com.halo.lims.model.Practitioner practitioner = user != null ? user.getPractitioner() : null;
        EncounterResponse response = encounterService.updateEncounterWorkflowStatus(id, status, practitioner);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR', 'PATHOLOGIST', 'RECEPTIONIST') and @securityService.canAccessEncounter(#id)")
    public ResponseEntity<EncounterResponse> getEncounterById(@PathVariable Integer id) {
        EncounterResponse response = encounterService.getEncounterById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR', 'PATHOLOGIST', 'RECEPTIONIST') and @securityService.canAccessEncounter(#id)")
    public ResponseEntity<EncounterDetailResponse> getEncounterDetailsById(@PathVariable Integer id) {
        try {
            log.debug("Fetching encounter details for ID: {}", id);
            EncounterDetailResponse response = encounterService.getEncounterDetailsById(id);
            log.debug("Successfully fetched encounter details for ID: {}", id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching encounter details for ID: {}", id, e);
            throw e; // Re-throw to let global exception handler handle it
        }
    }

    @GetMapping("/by-patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR', 'PATHOLOGIST', 'RECEPTIONIST') and @securityService.canAccessPatient(#patientId)")
    public ResponseEntity<List<EncounterResponse>> getEncountersByPatient(@PathVariable Integer patientId) {
        List<EncounterResponse> responses = encounterService.getEncountersByPatient(patientId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR', 'PATHOLOGIST', 'RECEPTIONIST') and @securityService.isUserInOrganization(#organizationId)")
    public ResponseEntity<PagedResponse<EncounterListResponse>> searchEncounters(
            @RequestParam("organizationId") Integer organizationId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "tests", required = false) List<Integer> testIds,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "sampleCollector", required = false) String sampleCollector,
            @RequestParam(value = "referringDoctor", required = false) String referringDoctor,
            @RequestParam(value = "hospital", required = false) String hospital,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "startTime") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") String sortDir) {
        if(StringUtils.isNotBlank(query)){
            query = query.trim();
        }
        if (StringUtils.isNotBlank(department)) {
            department = department.trim();
        }
        if (StringUtils.isNotBlank(sampleCollector)) {
            sampleCollector = sampleCollector.trim();
        }
        if (StringUtils.isNotBlank(referringDoctor)) {
            referringDoctor = referringDoctor.trim();
        }
        if (StringUtils.isNotBlank(hospital)) {
            hospital = hospital.trim();
        }

        PagedResponse<EncounterListResponse> response = encounterService.searchEncounters(
                organizationId,
                startDate,
                endDate,
                testIds,
                query,
                department,
                sampleCollector,
                referringDoctor,
                hospital,
                page,
                size,
                sortBy,
                sortDir
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

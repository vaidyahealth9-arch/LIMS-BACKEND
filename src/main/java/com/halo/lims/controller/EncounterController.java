package com.halo.lims.controller;

import com.halo.lims.dto.PagedResponse;
import com.halo.lims.dto.encounter.EncounterCreateRequest;
import com.halo.lims.dto.encounter.EncounterListResponse;
import com.halo.lims.dto.encounter.EncounterResponse;
import com.halo.lims.dto.encounter.EncounterUpdateRequest;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.EncounterService;
import jakarta.validation.Valid;
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

    private final EncounterService encounterService;
    private final SecurityService securityService; // Needed for @PreAuthorize checks

    public EncounterController(EncounterService encounterService, SecurityService securityService) {
        this.encounterService = encounterService;
        this.securityService = securityService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER') and @securityService.canAccessPatient(#request.patientId)")
    public ResponseEntity<EncounterResponse> createEncounter(@Valid @RequestBody EncounterCreateRequest request) {
        EncounterResponse response = encounterService.createEncounter(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER') and @securityService.canAccessEncounter(#id)")
    public ResponseEntity<EncounterResponse> updateEncounter(@PathVariable Integer id, @Valid @RequestBody EncounterUpdateRequest request) {
        EncounterResponse response = encounterService.updateEncounter(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'DOCTOR') and @securityService.canAccessEncounter(#id)")
    public ResponseEntity<EncounterResponse> getEncounterById(@PathVariable Integer id) {
        EncounterResponse response = encounterService.getEncounterById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/by-patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'DOCTOR') and @securityService.canAccessPatient(#patientId)")
    public ResponseEntity<List<EncounterResponse>> getEncountersByPatient(@PathVariable Integer patientId) {
        List<EncounterResponse> responses = encounterService.getEncountersByPatient(patientId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'DOCTOR') and @securityService.isUserInOrganization(#organizationId)")
    public ResponseEntity<PagedResponse<EncounterListResponse>> searchEncounters(
            @RequestParam("organizationId") Integer organizationId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "tests", required = false) List<Integer> testIds,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PagedResponse<EncounterListResponse> response = encounterService.searchEncounters(organizationId, startDate, endDate, testIds, query, page, size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

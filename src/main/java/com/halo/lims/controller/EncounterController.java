package com.halo.lims.controller;

import com.halo.lims.dto.encounter.EncounterCreateRequest;
import com.halo.lims.dto.encounter.EncounterResponse;
import com.halo.lims.dto.encounter.EncounterUpdateRequest;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.EncounterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER') and @securityService.canAccessPatient(#request.patientId)")
    public ResponseEntity<EncounterResponse> createEncounter(@Valid @RequestBody EncounterCreateRequest request) {
        EncounterResponse response = encounterService.createEncounter(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER') and @securityService.canAccessEncounter(#id)")
    public ResponseEntity<EncounterResponse> updateEncounter(@PathVariable Integer id, @Valid @RequestBody EncounterUpdateRequest request) {
        EncounterResponse response = encounterService.updateEncounter(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER', 'DOCTOR') and @securityService.canAccessEncounter(#id)")
    public ResponseEntity<EncounterResponse> getEncounterById(@PathVariable Integer id) {
        EncounterResponse response = encounterService.getEncounterById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/by-patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER', 'DOCTOR') and @securityService.canAccessPatient(#patientId)")
    public ResponseEntity<List<EncounterResponse>> getEncountersByPatient(@PathVariable Integer patientId) {
        List<EncounterResponse> responses = encounterService.getEncountersByPatient(patientId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

}

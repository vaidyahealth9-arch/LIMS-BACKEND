package com.halo.lims.controller;

import com.halo.lims.dto.PagedResponse;
import com.halo.lims.dto.patient.AbhaOtpVerificationRequest;
import com.halo.lims.dto.patient.PatientRegistrationRequest;
import com.halo.lims.dto.patient.PatientRegistrationResponse;
import com.halo.lims.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER', 'DOCTOR', 'TECHNICIAN') and @securityService.isUserInOrganization(#request.organizationId)")
    public ResponseEntity<PatientRegistrationResponse> registerPatient(@Valid @RequestBody PatientRegistrationRequest request) {

        PatientRegistrationResponse response = patientService.registerPatient(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{patientId}/abha/verify-otp")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER', 'ADMIN', 'TECHNICIAN')") // Same roles for ABHA linking
    public ResponseEntity<PatientRegistrationResponse> verifyAbhaOtpAndLink(
            @PathVariable Integer patientId,
            @Valid @RequestBody AbhaOtpVerificationRequest request) {
        // Add authorization check: Does this patient belong to the logged-in user's organization?
        PatientRegistrationResponse response = patientService.verifyAbhaAndLink(request, patientId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/by-organization/{organizationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER', 'DOCTOR', 'TECHNICIAN') and @securityService.isUserInOrganization(#organizationId)")
    public ResponseEntity<List<PatientRegistrationResponse>> getPatientsByOrganization(@PathVariable Integer organizationId) {
        List<PatientRegistrationResponse> patients = patientService.getPatientsByOrganization(organizationId);
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    @GetMapping("/by-organization/{organizationId}/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER', 'DOCTOR', 'TECHNICIAN') and @securityService.isUserInOrganization(#organizationId)")
    public ResponseEntity<PagedResponse<PatientRegistrationResponse>> searchPatientsInOrganization(
            @PathVariable Integer organizationId,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<PatientRegistrationResponse> patients = patientService.searchPatientsInOrganization(organizationId, query, page, size);
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    // TODO: Add endpoints for ABDM callbacks (Milestone 3 discovery)
}

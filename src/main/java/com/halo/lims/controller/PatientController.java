package com.halo.lims.controller;

import com.halo.lims.dto.PagedResponse;
import com.halo.lims.dto.patient.AbhaOtpVerificationRequest;
import com.halo.lims.dto.patient.PatientRegistrationRequest;
import com.halo.lims.dto.patient.PatientRegistrationResponse;
import com.halo.lims.service.PatientService;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'TECHNICIAN') and @securityService.isUserInOrganization(#request.organizationId)")
    public ResponseEntity<PatientRegistrationResponse> registerPatient(@Valid @RequestBody PatientRegistrationRequest request) {

        PatientRegistrationResponse response = patientService.registerPatient(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{patientId}/abha/verify-otp")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN', 'TECHNICIAN')") // Same roles for ABHA linking
    public ResponseEntity<PatientRegistrationResponse> verifyAbhaOtpAndLink(
            @PathVariable Integer patientId,
            @Valid @RequestBody AbhaOtpVerificationRequest request) {
        // Add authorization check: Does this patient belong to the logged-in user's organization?
        PatientRegistrationResponse response = patientService.verifyAbhaAndLink(request, patientId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/test-phr")
    public String testPhr(@RequestParam String mobile) {
        try {
            patientService.findPatientByMobile(mobile, "self");
            return "SUCCESS";
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            return sw.toString();
        }
    }

    @GetMapping("/by-organization/{organizationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'TECHNICIAN') and @securityService.isUserInOrganization(#organizationId)")
    public ResponseEntity<List<PatientRegistrationResponse>> getPatientsByOrganization(@PathVariable Integer organizationId) {
        List<PatientRegistrationResponse> patients = patientService.getPatientsByOrganization(organizationId);
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    @GetMapping("/phr-lookup")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'TECHNICIAN')")
    public ResponseEntity<PatientRegistrationResponse> lookupPatientFromPhr(
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String relationship,
            @RequestParam(required = false) String accessCode) {
        
        if (accessCode != null && !accessCode.isBlank()) {
            if (mobile == null || mobile.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Mobile number is required along with the access code.");
            }
            return patientService.findPatientByAccessCodeAndMobile(accessCode, mobile)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Access code or mobile number is invalid, expired, or already used. Please generate a new code in the PHR app."));
        }
        
        if (mobile == null || mobile.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mobile number is required");
        }

        return patientService.findPatientByMobile(mobile, relationship)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No patient details found for this mobile number with the specified profile"));

    }

    @GetMapping("/by-organization/{organizationId}/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'TECHNICIAN') and @securityService.isUserInOrganization(#organizationId)")
    public ResponseEntity<PagedResponse<PatientRegistrationResponse>> searchPatientsInOrganization(
            @PathVariable Integer organizationId,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if(StringUtils.isNotBlank(query)){
            query = query.trim();
        }
        PagedResponse<PatientRegistrationResponse> patients = patientService.searchPatientsInOrganization(organizationId, query, page, size);
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    // TODO: Add endpoints for ABDM callbacks (Milestone 3 discovery)
}

// Trigger DevTools restart

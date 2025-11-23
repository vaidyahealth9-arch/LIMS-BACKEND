package com.halo.lims.controller;

import com.halo.lims.dto.PagedResponse;
import com.halo.lims.dto.observation.ObservationCreateRequest;
import com.halo.lims.dto.observation.ObservationResponse;
import com.halo.lims.dto.serviceRequest.ServiceRequestCreateRequest;
import com.halo.lims.dto.serviceRequest.ServiceRequestObservationCreateRequest;
import com.halo.lims.dto.serviceRequest.ServiceRequestResponse;
import com.halo.lims.dto.serviceRequest.ServiceRequestUpdateRequest;
import com.halo.lims.security.CustomUserDetailsService;
import com.halo.lims.dto.serviceRequest.TestAnalytesResponse;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.ObservationService;
import com.halo.lims.service.ServiceRequestService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.halo.lims.dto.serviceRequest.AnalyteDetailResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/service-requests")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;
    private final SecurityService securityService;
    private final ObservationService observationService;
    private final CustomUserDetailsService customUserDetailsService;

    public ServiceRequestController(ServiceRequestService serviceRequestService, SecurityService securityService, ObservationService observationService, CustomUserDetailsService customUserDetailsService) {
        this.serviceRequestService = serviceRequestService;
        this.securityService = securityService;
        this.observationService = observationService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'RECEPTIONIST', 'MANAGER')")
    public ResponseEntity<ServiceRequestResponse> createServiceRequest(@Valid @RequestBody ServiceRequestCreateRequest request) {
        ServiceRequestResponse response = serviceRequestService.createServiceRequest(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id:[0-9]+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'RECEPTIONIST', 'MANAGER')")
    public ResponseEntity<ServiceRequestResponse> updateServiceRequest(@PathVariable Integer id, @Valid @RequestBody ServiceRequestUpdateRequest request) {
        ServiceRequestResponse response = serviceRequestService.updateServiceRequest(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id:[0-9]+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER', 'DOCTOR', 'TECHNICIAN')")
    public ResponseEntity<ServiceRequestResponse> getServiceRequestById(@PathVariable Integer id) {
        ServiceRequestResponse response = serviceRequestService.getServiceRequestById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/by-patient/{patientId:[0-9]+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER', 'DOCTOR', 'TECHNICIAN')")
    public ResponseEntity<List<ServiceRequestResponse>> getServiceRequestsByPatient(@PathVariable Integer patientId) {
        List<ServiceRequestResponse> responses = serviceRequestService.getServiceRequestsByPatient(patientId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'RECEPTIONIST', 'MANAGER')")
    public ResponseEntity<PagedResponse<ServiceRequestResponse>> getPendingServiceRequests(
            @RequestParam(required = false) Integer orgId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<ServiceRequestResponse> response = serviceRequestService.getPendingServiceRequests(orgId, startDate, endDate, page, size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{id:[0-9]+}/observations")
    @PreAuthorize("hasRole('TECHNICIAN') and @securityService.canAccessServiceRequest(#id)")
    public ResponseEntity<ObservationResponse> createObservationForServiceRequest(
            @PathVariable Integer id,
            @Valid @RequestBody ServiceRequestObservationCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer performerId = customUserDetailsService.getPractitionerIdFromUserDetails(userDetails);

        ObservationCreateRequest observationCreateRequest = new ObservationCreateRequest();
        observationCreateRequest.setServiceRequestId(id);
        observationCreateRequest.setSpecimenId(request.getSpecimenId());
        observationCreateRequest.setAnalyteId(request.getAnalyteId());
        observationCreateRequest.setValueNumeric(request.getValueNumeric());
        observationCreateRequest.setValueString(request.getValueString());
        observationCreateRequest.setValueCode(request.getValueCode());
        observationCreateRequest.setValueCodeSystem(request.getValueCodeSystem());
        observationCreateRequest.setInterpretationCode(request.getInterpretationCode());
        observationCreateRequest.setInterpretationSystem(request.getInterpretationSystem());
        observationCreateRequest.setEffectiveDateTime(request.getEffectiveDateTime());

        ObservationResponse response = observationService.createObservation(observationCreateRequest, performerId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{serviceRequestId:[0-9]+}/observations")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER', 'DOCTOR', 'TECHNICIAN') and @securityService.canAccessServiceRequest(#serviceRequestId)")
    public ResponseEntity<List<ObservationResponse>> getObservationsByServiceRequestId(@PathVariable Integer serviceRequestId) {
        List<ObservationResponse> responses = observationService.getObservationsByServiceRequestId(serviceRequestId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/{id:[0-9]+}/analytes")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER', 'DOCTOR', 'TECHNICIAN') and @securityService.canAccessServiceRequest(#id)")
    public ResponseEntity<List<TestAnalytesResponse>> getServiceRequestAnalytes(@PathVariable Integer id) {
        List<TestAnalytesResponse> response = serviceRequestService.getServiceRequestAnalytes(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

//    http://localhost:3000/api/service-requests/search
}
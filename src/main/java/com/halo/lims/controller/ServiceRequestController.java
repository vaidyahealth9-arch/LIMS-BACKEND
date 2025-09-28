package com.halo.lims.controller;

import com.halo.lims.dto.serviceRequest.ServiceRequestCreateRequest;
import com.halo.lims.dto.serviceRequest.ServiceRequestResponse;
import com.halo.lims.dto.serviceRequest.ServiceRequestUpdateRequest;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.ServiceRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-requests")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;
    private final SecurityService securityService;

    public ServiceRequestController(ServiceRequestService serviceRequestService, SecurityService securityService) {
        this.serviceRequestService = serviceRequestService;
        this.securityService = securityService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'RECEPTIONIST', 'MANAGER')")
    public ResponseEntity<ServiceRequestResponse> createServiceRequest(@Valid @RequestBody ServiceRequestCreateRequest request) {
        ServiceRequestResponse response = serviceRequestService.createServiceRequest(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'RECEPTIONIST', 'MANAGER')")
    public ResponseEntity<ServiceRequestResponse> updateServiceRequest(@PathVariable Integer id, @Valid @RequestBody ServiceRequestUpdateRequest request) {
        ServiceRequestResponse response = serviceRequestService.updateServiceRequest(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER', 'DOCTOR', 'TECHNICIAN')")
    public ResponseEntity<ServiceRequestResponse> getServiceRequestById(@PathVariable Integer id) {
        ServiceRequestResponse response = serviceRequestService.getServiceRequestById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/by-patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER', 'DOCTOR', 'TECHNICIAN')")
    public ResponseEntity<List<ServiceRequestResponse>> getServiceRequestsByPatient(@PathVariable Integer patientId) {
        List<ServiceRequestResponse> responses = serviceRequestService.getServiceRequestsByPatient(patientId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}

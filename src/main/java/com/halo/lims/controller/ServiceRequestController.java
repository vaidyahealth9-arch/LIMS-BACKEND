package com.halo.lims.controller;

import com.halo.lims.dto.PagedResponse;
import com.halo.lims.dto.serviceRequest.ServiceRequestCreateRequest;
import com.halo.lims.dto.serviceRequest.ServiceRequestResponse;
import com.halo.lims.dto.serviceRequest.ServiceRequestUpdateRequest;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.ServiceRequestService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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
//    http://localhost:3000/api/service-requests/search
}

package com.halo.lims.service;

import com.halo.lims.dto.serviceRequest.ServiceRequestCreateRequest;
import com.halo.lims.dto.serviceRequest.ServiceRequestResponse;
import com.halo.lims.dto.serviceRequest.ServiceRequestUpdateRequest;
import com.halo.lims.model.*;
import com.halo.lims.repository.*;
import com.halo.lims.security.SecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestItemRepository serviceRequestItemRepository; // For individual tests within a request
    private final PatientRepository patientRepository;
    private final PractitionerRepository practitionerRepository;
    private final EncounterRepository encounterRepository;
    private final TestRepository testRepository;
    private final OrganizationTestRepository organizationTestRepository; // For lab-specific test catalog
    private final SecurityService securityService;

    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository,
                                 ServiceRequestItemRepository serviceRequestItemRepository,
                                 PatientRepository patientRepository,
                                 PractitionerRepository practitionerRepository,
                                 EncounterRepository encounterRepository,
                                 TestRepository testRepository,
                                 OrganizationTestRepository organizationTestRepository,
                                 SecurityService securityService) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.serviceRequestItemRepository = serviceRequestItemRepository;
        this.patientRepository = patientRepository;
        this.practitionerRepository = practitionerRepository;
        this.encounterRepository = encounterRepository;
        this.testRepository = testRepository;
        this.organizationTestRepository = organizationTestRepository;
        this.securityService = securityService;
    }

    @Transactional
    public ServiceRequestResponse createServiceRequest(ServiceRequestCreateRequest request) {

        Practitioner requester = practitionerRepository.findById(request.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester Practitioner not found with ID: " + request.getRequesterId()));
        Encounter encounter = null;
        Patient patient = null;
        if (request.getEncounterId() != null) {
            encounter = encounterRepository.findById(request.getEncounterId())
                    .orElseThrow(() -> new RuntimeException("Encounter not found with ID: " + request.getEncounterId()));
        }
        if(Objects.nonNull(encounter)){
            patient = encounter.getPatient();
        }
        else if(Objects.nonNull(request.getPatientId())){
            patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + request.getPatientId()));
        }

        if(Objects.isNull(patient)){
            throw new RuntimeException("Patient not found with given encounter / patient ID ");
        }


        // --- Multi-tenancy check ---
        Integer organizationId = patient.getOrganization().getId();
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to create service requests for patients in organization ID: " + organizationId);
        }
        // Also check if requester belongs to the same organization, or is an external referring practitioner
        // For simplicity, assuming requester can be external, but patient's org defines context.
        // Or if requester is an internal user, check isUserInOrganization(requester.getOrganization().getId())
        // --- End multi-tenancy check ---

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .patient(patient)
                .requester(requester)
                .encounter(encounter)
                .orderDate(OffsetDateTime.now())
                .status(request.getStatus())
                .priority(request.getPriority())
                .localOrderSystem("http://com.lims/service-request-id")
                .localOrderValue(generateLocalServiceRequestId())
                .build();

        ServiceRequest savedServiceRequest = serviceRequestRepository.save(serviceRequest);

        // Add ServiceRequestItems (individual tests)
        List<ServiceRequestItem> items = request.getTestIds().stream()
                .map(testId -> {
                    Test test = testRepository.findById(testId)
                            .orElseThrow(() -> new RuntimeException("Test not found with ID: " + testId));

                    // --- Lab-specific Test Catalog Check (CRITICAL) ---
                    OrganizationTest orgTest = organizationTestRepository.findByOrganization_IdAndTest_Id(organizationId, testId)
                            .orElseThrow(() -> new RuntimeException("Test '" + test.getTestName() + "' (ID: " + testId + ") is not configured for organization ID: " + organizationId));
                    if (!orgTest.getIsEnabled()) {
                        throw new IllegalArgumentException("Test '" + test.getTestName() + "' (ID: " + testId + ") is disabled for organization ID: " + organizationId);
                    }
                    // --- End Lab-specific Test Catalog Check ---

                    return ServiceRequestItem.builder()
                            .serviceRequest(savedServiceRequest)
                            .test(test)
                            .panel(null) // Assuming individual tests, not panels
                            .status("requested")
                            .build();
                })
                .collect(Collectors.toList());
        serviceRequestItemRepository.saveAll(items);

        return mapToServiceRequestResponse(savedServiceRequest);
    }

    @Transactional
    public ServiceRequestResponse updateServiceRequest(Integer id, ServiceRequestUpdateRequest request) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service Request not found with ID: " + id));

        // --- Multi-tenancy check ---
        Integer organizationId = serviceRequest.getPatient().getOrganization().getId();
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to update service requests for patients in organization ID: " + organizationId);
        }
        // --- End multi-tenancy check ---

        // Prevent modification if already completed/cancelled
        if ("completed".equals(serviceRequest.getStatus()) || "cancelled".equals(serviceRequest.getStatus())) {
            throw new IllegalStateException("Cannot update a completed or cancelled service request.");
        }

        if (request.getRequesterId() != null) {
            Practitioner requester = practitionerRepository.findById(request.getRequesterId())
                    .orElseThrow(() -> new RuntimeException("Requester Practitioner not found with ID: " + request.getRequesterId()));
            serviceRequest.setRequester(requester);
        }
        if (request.getEncounterId() != null) {
            Encounter encounter = encounterRepository.findById(request.getEncounterId())
                    .orElseThrow(() -> new RuntimeException("Encounter not found with ID: " + request.getEncounterId()));
            if (!encounter.getPatient().getId().equals(serviceRequest.getPatient().getId())) {
                throw new IllegalArgumentException("Encounter does not belong to the specified patient.");
            }
            serviceRequest.setEncounter(encounter);
        }
        if (request.getStatus() != null) serviceRequest.setStatus(request.getStatus());
        if (request.getPriority() != null) serviceRequest.setPriority(request.getPriority());

        // TODO: Handle testIds update (adding/removing tests). This requires careful logic
        // to avoid deleting items that already have specimens/observations.
        // For simplicity, current implementation only creates items, not removes.

        ServiceRequest updatedServiceRequest = serviceRequestRepository.save(serviceRequest);
        return mapToServiceRequestResponse(updatedServiceRequest);
    }

    @Transactional(readOnly = true)
    public ServiceRequestResponse getServiceRequestById(Integer id) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service Request not found with ID: " + id));

        // --- Multi-tenancy check ---
        Integer organizationId = serviceRequest.getPatient().getOrganization().getId();
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to view service requests for patients in organization ID: " + organizationId);
        }
        // --- End multi-tenancy check ---

        return mapToServiceRequestResponse(serviceRequest);
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getServiceRequestsByPatient(Integer patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));

        // --- Multi-tenancy check ---
        Integer organizationId = patient.getOrganization().getId();
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to view service requests for patients in organization ID: " + organizationId);
        }
        // --- End multi-tenancy check ---

        return serviceRequestRepository.findByPatient(patient).stream()
                .map(this::mapToServiceRequestResponse)
                .collect(Collectors.toList());
    }

    private String generateLocalServiceRequestId() {
        return "SR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private ServiceRequestResponse mapToServiceRequestResponse(ServiceRequest serviceRequest) {
        ServiceRequestResponse response = new ServiceRequestResponse();
        response.setId(serviceRequest.getId());
        response.setLocalOrderValue(serviceRequest.getLocalOrderValue());
        response.setPatientId(serviceRequest.getPatient().getId());
        response.setPatientMrn(serviceRequest.getPatient().getLocalMrnValue());
        response.setPatientName(serviceRequest.getPatient().getFirstName() + " " + serviceRequest.getPatient().getLastName());
        response.setRequesterId(serviceRequest.getRequester().getId());
        response.setRequesterName(serviceRequest.getRequester().getFirstName() + " " + serviceRequest.getRequester().getLastName());
        if (serviceRequest.getEncounter() != null) {
            response.setEncounterId(serviceRequest.getEncounter().getId());
            response.setEncounterLocalValue(serviceRequest.getEncounter().getLocalEncounterValue());
        }
        response.setOrderDate(serviceRequest.getOrderDate());
        response.setStatus(serviceRequest.getStatus());
        response.setPriority(serviceRequest.getPriority());
        response.setOrganizationId(serviceRequest.getPatient().getOrganization().getId());
        response.setOrganizationName(serviceRequest.getPatient().getOrganization().getOrganizationName());

        List<ServiceRequestResponse.TestDetailsResponse> requestedTests = serviceRequestItemRepository.findByServiceRequest(serviceRequest).stream()
                .map(item -> {
                    ServiceRequestResponse.TestDetailsResponse testDetails = new ServiceRequestResponse.TestDetailsResponse();
                    testDetails.setTestId(item.getTest().getId());
                    testDetails.setTestLocalCode(item.getTest().getLocalCode());
                    testDetails.setTestName(item.getTest().getTestName());
                    testDetails.setStatus(item.getStatus());
                    // Fetch the price at the time of order from OrganizationTest if available
                    BigDecimal price = organizationTestRepository.findByOrganization_IdAndTest_Id(serviceRequest.getPatient().getOrganization().getId(), item.getTest().getId())
                            .map(OrganizationTest::getPrice)
                            .orElse(null); // Or default price if not found
                    testDetails.setPrice(price);
                    return testDetails;
                })
                .collect(Collectors.toList());
        response.setRequestedTests(requestedTests);

        response.setCreatedAt(serviceRequest.getCreatedAt());
        response.setUpdatedAt(serviceRequest.getUpdatedAt());
        return response;
    }
}

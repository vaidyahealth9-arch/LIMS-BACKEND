package com.halo.lims.service;

import com.halo.lims.constant.ServiceRequestStatus;
import com.halo.lims.dto.PagedResponse;
import com.halo.lims.dto.serviceRequest.ServiceRequestCreateRequest;
import com.halo.lims.dto.serviceRequest.TestSpecimenRequest;
import com.halo.lims.dto.specimen.SpecimenCreateRequest;
import com.halo.lims.dto.serviceRequest.ServiceRequestResponse;
import com.halo.lims.dto.serviceRequest.ServiceRequestUpdateRequest;
import com.halo.lims.model.*;
import com.halo.lims.repository.*;
import com.halo.lims.security.SecurityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
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
    private final TestAnalyteRepository testAnalyteRepository;
    private final ReferenceRangeRepository referenceRangeRepository;
    private final SpecimenService specimenService;

    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository,
                                 ServiceRequestItemRepository serviceRequestItemRepository,
                                 PatientRepository patientRepository,
                                 PractitionerRepository practitionerRepository,
                                 EncounterRepository encounterRepository,
                                 TestRepository testRepository,
                                 OrganizationTestRepository organizationTestRepository,
                                 SecurityService securityService, TestAnalyteRepository testAnalyteRepository, ReferenceRangeRepository referenceRangeRepository, SpecimenService specimenService) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.serviceRequestItemRepository = serviceRequestItemRepository;
        this.patientRepository = patientRepository;
        this.practitionerRepository = practitionerRepository;
        this.encounterRepository = encounterRepository;
        this.testRepository = testRepository;
        this.organizationTestRepository = organizationTestRepository;
        this.securityService = securityService;
        this.testAnalyteRepository = testAnalyteRepository;
        this.referenceRangeRepository = referenceRangeRepository;
        this.specimenService = specimenService;
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
                .status(request.getStatus().toLowerCase())
                .priority(request.getPriority())
                .localOrderSystem("http://com.lims/service-request-id")
                .localOrderValue(generateLocalServiceRequestId())
                .build();

        ServiceRequest savedServiceRequest = serviceRequestRepository.save(serviceRequest);

        // Add ServiceRequestItems (individual tests)
        List<ServiceRequestItem> items = new ArrayList<>();
        Map<Integer, List<String>> testSpecimenBarcodes = new HashMap<>();
        if (request.getTests() != null) {
            for (TestSpecimenRequest testSpecimenRequest : request.getTests()) {
                Test test = testRepository.findById(testSpecimenRequest.getTestId())
                        .orElseThrow(() -> new RuntimeException("Test not found with ID: " + testSpecimenRequest.getTestId()));

                // --- Lab-specific Test Catalog Check (CRITICAL) ---
                OrganizationTest orgTest = organizationTestRepository.findByOrganization_IdAndTest_Id(organizationId, testSpecimenRequest.getTestId())
                        .orElseThrow(() -> new RuntimeException("Test '" + test.getTestName() + "' (ID: " + testSpecimenRequest.getTestId() + ") is not configured for organization ID: " + organizationId));
                if (!orgTest.getIsEnabled()) {
                    throw new IllegalArgumentException("Test '" + test.getTestName() + "' (ID: " + testSpecimenRequest.getTestId() + ") is disabled for organization ID: " + organizationId);
                }
                // --- End Lab-specific Test Catalog Check ---

                ServiceRequestItem item = ServiceRequestItem.builder()
                        .serviceRequest(savedServiceRequest)
                        .test(test)
                        .panel(null) // Assuming individual tests, not panels
                        .status("requested")
                        .build();
                items.add(item);

                List<String> barcodes = new ArrayList<>();
                // Create specimens for this test
                for (int i = 0; i < testSpecimenRequest.getNumberOfSpecimens(); i++) {
                    SpecimenCreateRequest specimenCreateRequest = new SpecimenCreateRequest();
                    specimenCreateRequest.setServiceRequestId(savedServiceRequest.getId());
                    specimenCreateRequest.setSpecimenTypeId(testSpecimenRequest.getSpecimenTypeId());
                    specimenCreateRequest.setCollectionDate(OffsetDateTime.now()); // Defaulting collection date
                    specimenCreateRequest.setStatus("unavailable"); // Defaulting status
                    Specimen createdSpecimen = specimenService.createSpecimen(specimenCreateRequest);
                    barcodes.add(createdSpecimen.getBarcode());
                }
                testSpecimenBarcodes.put(test.getId(), barcodes);
            }
        }
        serviceRequestItemRepository.saveAll(items);

        return mapToServiceRequestResponse(savedServiceRequest, testSpecimenBarcodes);
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

        if (request.getTests() != null && !request.getTests().isEmpty()) {
            List<Integer> existingTestIds = serviceRequestItemRepository.findByServiceRequest(serviceRequest)
                    .stream()
                    .map(item -> item.getTest().getId())
                    .toList();

            List<TestSpecimenRequest> newTests = request.getTests().stream()
                    .filter(testSpecimenRequest -> !existingTestIds.contains(testSpecimenRequest.getTestId()))
                    .toList();

            if (!newTests.isEmpty()) {
                List<ServiceRequestItem> newItems = new ArrayList<>();
                for (TestSpecimenRequest testSpecimenRequest : newTests) {
                    Test test = testRepository.findById(testSpecimenRequest.getTestId())
                            .orElseThrow(() -> new RuntimeException("Test not found with ID: " + testSpecimenRequest.getTestId()));

                    OrganizationTest orgTest = organizationTestRepository.findByOrganization_IdAndTest_Id(organizationId, testSpecimenRequest.getTestId())
                            .orElseThrow(() -> new RuntimeException("Test '" + test.getTestName() + "' (ID: " + testSpecimenRequest.getTestId() + ") is not configured for organization ID: " + organizationId));
                    if (!orgTest.getIsEnabled()) {
                        throw new IllegalArgumentException("Test '" + test.getTestName() + "' (ID: " + testSpecimenRequest.getTestId() + ") is disabled for organization ID: " + organizationId);
                    }

                    ServiceRequestItem item = ServiceRequestItem.builder()
                            .serviceRequest(serviceRequest)
                            .test(test)
                            .panel(null)
                            .status("requested")
                            .build();
                    newItems.add(item);

                    // Create specimens for this test
                    for (int i = 0; i < testSpecimenRequest.getNumberOfSpecimens(); i++) {
                        SpecimenCreateRequest specimenCreateRequest = new SpecimenCreateRequest();
                        specimenCreateRequest.setServiceRequestId(serviceRequest.getId());
                        specimenCreateRequest.setSpecimenTypeId(testSpecimenRequest.getSpecimenTypeId());
                        specimenCreateRequest.setCollectionDate(OffsetDateTime.now()); // Defaulting collection date
                        specimenCreateRequest.setStatus("unavailable"); // Defaulting status
                        specimenService.createSpecimen(specimenCreateRequest);
                    }
                }
                serviceRequestItemRepository.saveAll(newItems);
            }
        }

        ServiceRequest updatedServiceRequest = serviceRequestRepository.save(serviceRequest);
        return mapToServiceRequestResponse(updatedServiceRequest, java.util.Collections.emptyMap());
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

        return mapToServiceRequestResponse(serviceRequest, java.util.Collections.emptyMap());
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
                .map(serviceRequest -> mapToServiceRequestResponse(serviceRequest, java.util.Collections.emptyMap()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<ServiceRequestResponse> getPendingServiceRequests(
            Integer orgId, LocalDate startDate, LocalDate endDate, int page, int size) {

        // --- Multi-tenancy check ---
        if (orgId != null && !securityService.isUserInOrganization(orgId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to view service requests for organization ID: " + orgId);
        }
        // If orgId is null, the specification will fetch for all orgs the user has access to.
        // This requires a more complex check, for now we assume orgId is mandatory if user is not an admin.
        // A better approach would be to get a list of user's orgs from SecurityService and add an IN clause.
        // --- End multi-tenancy check ---

        Pageable pageable = PageRequest.of(page, size);

        Specification<ServiceRequest> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Always filter by "active" status
            predicates.add(cb.equal(root.get("status"), ServiceRequestStatus.ACTIVE.getCode()));

            // Filter by organization
            if (orgId != null) {
                predicates.add(cb.equal(root.get("patient").get("organization").get("id"), orgId));
            }

            // Filter by date range
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), endDate));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<ServiceRequest> serviceRequestPage = serviceRequestRepository.findAll(spec, pageable);

        List<ServiceRequestResponse> serviceRequestResponses = serviceRequestPage.getContent().stream()
                .map(serviceRequest -> mapToServiceRequestResponse(serviceRequest, java.util.Collections.emptyMap()))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                serviceRequestResponses,
                serviceRequestPage.getNumber(),
                serviceRequestPage.getSize(),
                (int) serviceRequestPage.getTotalElements(),
                serviceRequestPage.getTotalPages()
        );
    }

    private String generateLocalServiceRequestId() {
        return "SR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private ServiceRequestResponse mapToServiceRequestResponse(ServiceRequest serviceRequest, Map<Integer, List<String>> testSpecimenBarcodes) {
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
                    testDetails.setSpecimenBarcodes(testSpecimenBarcodes.get(item.getTest().getId()));

                    List<TestAnalyte> analytes = testAnalyteRepository.findByParentTestId(item.getTest().getId());
                    List<ServiceRequestResponse.AnalyteDetailsResponse> analyteDetailsResponses = analytes.stream()
                            .map(analyte -> {
                                ServiceRequestResponse.AnalyteDetailsResponse analyteDetails = new ServiceRequestResponse.AnalyteDetailsResponse();
                                analyteDetails.setAnalyteId(analyte.getId());
                                analyteDetails.setAnalyteName(analyte.getAnalyteName());
                                if (analyte.getUnit() != null) {
                                    analyteDetails.setUnit(analyte.getUnit().getName());
                                }
                                List<ServiceRequestResponse.ReferenceRangeResponse> referenceRangeResponses = referenceRangeRepository.findByAnalyte(analyte).stream()
                                        .map(rr -> {
                                            ServiceRequestResponse.ReferenceRangeResponse rrDetails = new ServiceRequestResponse.ReferenceRangeResponse();
                                            rrDetails.setId(rr.getId());
                                            rrDetails.setGender(rr.getGender());
                                            rrDetails.setMinAgeYears(rr.getMinAgeYears());
                                            rrDetails.setMaxAgeYears(rr.getMaxAgeYears());
                                            rrDetails.setLowValue(rr.getLowValue());
                                            rrDetails.setHighValue(rr.getHighValue());
                                            rrDetails.setTextRange(rr.getTextRange());
                                            rrDetails.setInterpretationCode(rr.getInterpretationCode());
                                            return rrDetails;
                                        }).collect(Collectors.toList());
                                analyteDetails.setReferenceRanges(referenceRangeResponses);
                                return analyteDetails;
                            }).collect(Collectors.toList());
                    testDetails.setAnalytes(analyteDetailsResponses);

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

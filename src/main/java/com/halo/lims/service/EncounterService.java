package com.halo.lims.service;

import com.halo.lims.constant.EncounterStatus;
import com.halo.lims.constant.ServiceRequestStatus;
import com.halo.lims.dto.PagedResponse;
import com.halo.lims.dto.encounter.EncounterCreateRequest;
import com.halo.lims.dto.encounter.EncounterDetailResponse;
import com.halo.lims.dto.encounter.EncounterListResponse;
import com.halo.lims.dto.encounter.EncounterResponse;
import com.halo.lims.dto.encounter.EncounterUpdateRequest;
import com.halo.lims.security.SecurityService;
import com.halo.lims.model.Encounter;
import com.halo.lims.model.Organization;
import com.halo.lims.model.Patient;
import com.halo.lims.model.Bill;
import com.halo.lims.model.ServiceRequest;
import com.halo.lims.model.ServiceRequestItem;
import com.halo.lims.repository.BillRepository;
import com.halo.lims.repository.EncounterRepository;
import com.halo.lims.repository.OrganizationRepository;
import com.halo.lims.repository.PatientRepository;
import com.halo.lims.repository.ServiceRequestItemRepository;
import com.halo.lims.repository.ServiceRequestRepository;
import com.halo.lims.repository.SpecimenRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EncounterService {

    private static final Logger log = LoggerFactory.getLogger(EncounterService.class);
    
    private final EncounterRepository encounterRepository;
    private final PatientRepository patientRepository;
    private final OrganizationRepository organizationRepository;
    private final SecurityService securityService;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestItemRepository serviceRequestItemRepository;
    private final SpecimenRepository specimenRepository;
    private final BillRepository billRepository;
    private final ReportApprovalService reportApprovalService;
    private final ReportService reportService;
    private final IdentifierGenerationService identifierGenerationService;

    public EncounterService(EncounterRepository encounterRepository,
                            PatientRepository patientRepository,
                            OrganizationRepository organizationRepository,
                            SecurityService securityService,
                            ServiceRequestRepository serviceRequestRepository,
                            ServiceRequestItemRepository serviceRequestItemRepository,
                            SpecimenRepository specimenRepository,
                            BillRepository billRepository,
                            ReportApprovalService reportApprovalService,
                            ReportService reportService,
                            IdentifierGenerationService identifierGenerationService) {
        this.encounterRepository = encounterRepository;
        this.patientRepository = patientRepository;
        this.organizationRepository = organizationRepository;
        this.securityService = securityService;
        this.serviceRequestRepository = serviceRequestRepository;
        this.serviceRequestItemRepository = serviceRequestItemRepository;
        this.specimenRepository = specimenRepository;
        this.billRepository = billRepository;
        this.reportApprovalService = reportApprovalService;
        this.reportService = reportService;
        this.identifierGenerationService = identifierGenerationService;
    }

    @Transactional
    public EncounterResponse createEncounter(EncounterCreateRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + request.getPatientId()));
        
        Organization serviceProvider = organizationRepository.findById(request.getServiceProviderId())
            .orElseGet(patient::getOrganization);

        if (serviceProvider == null || serviceProvider.getId() == null) {
            throw new RuntimeException("Service Provider Organization not found with ID: " + request.getServiceProviderId());
        }

        // --- Multi-tenancy check ---
        Integer patientOrgId = resolveOrganizationId(patient.getOrganization(), "patient", patient.getId());
        if (!securityService.isUserInOrganization(patientOrgId)) {
            throw new AccessDeniedException("User not authorized to create encounters for patients in organization ID: " + patientOrgId);
        }
        if (!securityService.isUserInOrganization(serviceProvider.getId())) {
            throw new AccessDeniedException("User not authorized to create encounters with service provider organization ID: " + serviceProvider.getId());
        }
        // --- End multi-tenancy check ---

        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setStartTime(request.getStartTime());
        encounter.setEndTime(request.getEndTime());
        encounter.setStatus(request.getStatus() != null ? normalizeEncounterStatusCode(request.getStatus()) : null);
        encounter.setEncounterClass(request.getEncounterClass());
        encounter.setServiceProvider(serviceProvider);
        encounter.setLocalEncounterSystem("http://com.lims/encounter-id");
        encounter.setLocalEncounterValue(identifierGenerationService.generateEncounterValue(serviceProvider.getId(), 3));
        encounter.setReferenceDoctor(request.getReferenceDoctor());

        Encounter savedEncounter = encounterRepository.save(encounter);
        return mapToEncounterResponse(savedEncounter);
    }

    @Transactional
    public EncounterResponse updateEncounter(Integer id, EncounterUpdateRequest request) {
        Encounter encounter = encounterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encounter not found with ID: " + id));

        // --- Multi-tenancy check ---
        Integer patientOrgId = resolveEncounterOrganizationId(encounter);
        if (!securityService.isUserInOrganization(patientOrgId)) {
            throw new AccessDeniedException("User not authorized to update encounters for patients in organization ID: " + patientOrgId);
        }
        // --- End multi-tenancy check ---

        if (request.getEndTime() != null) encounter.setEndTime(request.getEndTime());

        if (request.getStatus() != null) {
            String requestedStatus = normalizeEncounterStatusCode(request.getStatus());
            validateEncounterStatusChange(encounter, requestedStatus);
            encounter.setStatus(requestedStatus);
        }

        if (request.getEncounterClass() != null) encounter.setEncounterClass(request.getEncounterClass());

        Encounter updatedEncounter = encounterRepository.save(encounter);
        return mapToEncounterResponse(updatedEncounter);
    }

    @Transactional
    public EncounterResponse updateEncounterWorkflowStatus(Integer id, String newStatus, com.halo.lims.model.Practitioner approver) {
        Encounter encounter = encounterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encounter not found with ID: " + id));

        // --- Multi-tenancy check ---
        Integer patientOrgId = resolveEncounterOrganizationId(encounter);
        if (!securityService.isUserInOrganization(patientOrgId)) {
            throw new AccessDeniedException("User not authorized to update encounters for patients in organization ID: " + patientOrgId);
        }

        String normalizedStatus = normalizeEncounterStatusCode(newStatus);
        validateEncounterStatusChange(encounter, normalizedStatus);

        if (EncounterStatus.APPROVED.getCode().equalsIgnoreCase(normalizedStatus)) {
            throw new IllegalStateException("Doctor approval must be completed from the observation approval workflow. Encounter approval is assigned automatically after results are finalized.");
        }

        if (EncounterStatus.COMPLETED.getCode().equalsIgnoreCase(normalizedStatus)) {
            List<Bill> bills = billRepository.findByEncounter(encounter);
            boolean hasOutstandingBalance = bills.stream().anyMatch(bill -> bill.getDueAmount() != null && bill.getDueAmount().compareTo(java.math.BigDecimal.ZERO) > 0);
            if (hasOutstandingBalance) {
                throw new IllegalStateException("Cannot complete encounter. Full payment is still pending.");
            }
        }

        encounter.setStatus(normalizedStatus);
        if (approver != null && EncounterStatus.APPROVED.getCode().equalsIgnoreCase(normalizedStatus)) {
            encounter.setApprovingPractitioner(approver);
        }

        // Propagate COMPLETED status to all associated ServiceRequests
        if (EncounterStatus.COMPLETED.getCode().equalsIgnoreCase(normalizedStatus)) {
            List<ServiceRequest> serviceRequests = serviceRequestRepository.findByEncounter(encounter);
            for (ServiceRequest sr : serviceRequests) {
                if (sr == null || sr.getId() == null) {
                    continue;
                }
                reportApprovalService.getReportApprovalStatus(sr.getId());
                reportService.buildUnifiedPdfReport(sr.getId(), true, "regular");
            }
            for (ServiceRequest sr : serviceRequests) {
                if (sr.getStatus() != null && !"cancelled".equalsIgnoreCase(sr.getStatus())) {
                    sr.setStatus(ServiceRequestStatus.COMPLETED.getCode());
                    serviceRequestRepository.save(sr);
                }
            }
        }

        Encounter saved = encounterRepository.save(encounter);
        return mapToEncounterResponse(saved);
    }

    private void validateEncounterStatusChange(Encounter encounter, String newStatus) {
        String currentStatus = encounter.getStatus();
        if (EncounterStatus.COMPLETED.getCode().equalsIgnoreCase(currentStatus)) {
            throw new IllegalStateException("Cannot change status of a COMPLETED encounter");
        }
        if (EncounterStatus.CANCELLED.getCode().equalsIgnoreCase(currentStatus)) {
            throw new IllegalStateException("Cannot change status of a CANCELLED encounter");
        }
    }

    @Transactional(readOnly = true)
    public EncounterResponse getEncounter(Integer id) {
        Encounter encounter = encounterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encounter not found with ID: " + id));
        
        // Multi-tenancy check
        Integer organizationId = resolveEncounterOrganizationId(encounter);
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new AccessDeniedException("User not authorized to access encounter ID: " + id);
        }

        return mapToEncounterResponse(encounter);
    }

    @Transactional(readOnly = true)
    public EncounterDetailResponse getEncounterDetail(Integer id) {
        log.info("Attempting to get encounter details for ID: {}", id);
        
        Encounter encounter = encounterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encounter not found with ID: " + id));
        
        log.debug("Encounter fetched: ID={}, PatientId={}, ServiceProviderId={}", 
                  id, encounter.getPatient() != null ? encounter.getPatient().getId() : null,
                  encounter.getServiceProvider() != null ? encounter.getServiceProvider().getId() : null);

        // Multi-tenancy check
        Integer organizationId = resolveEncounterOrganizationId(encounter);
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new AccessDeniedException("User not authorized to access encounter ID: " + id);
        }

        List<ServiceRequest> serviceRequests = new ArrayList<>();
        try {
            serviceRequests = serviceRequestRepository.findByEncounter(encounter);
            log.debug("Found {} service requests for encounter ID: {}", serviceRequests.size(), id);
        } catch (Exception e) {
            log.error("Error fetching service requests for encounter ID: {}", id, e);
            // Continue with empty list rather than failing completely
        }

        List<ServiceRequestItem> items = new ArrayList<>();
        try {
            if (!serviceRequests.isEmpty()) {
                items = serviceRequestItemRepository.findByServiceRequestIn(serviceRequests);
                log.debug("Found {} service request items for encounter ID: {}", items.size(), id);
            }
        } catch (Exception e) {
            log.error("Error fetching service request items for encounter ID: {}", id, e);
            // Continue with empty list rather than failing completely
        }
        
        List<String> testNames = items.stream()
            .map(this::safeServiceRequestItemTestName)
            .filter(name -> name != null && !name.isBlank())
                .distinct()
                .collect(Collectors.toList());

        // Fetch specimen barcodes for all service requests
        List<String> specimenBarcodes = new ArrayList<>();
        if (!serviceRequests.isEmpty()) {
            try {
                specimenBarcodes = specimenRepository.findByServiceRequestIn(serviceRequests).stream()
                        .map(specimen -> specimen.getBarcode())
                        .filter(barcode -> barcode != null && !barcode.trim().isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
                log.debug("Found {} specimen barcodes for encounter ID: {}", specimenBarcodes.size(), id);
            } catch (Exception e) {
                log.error("Error fetching specimen barcodes for encounter ID: {}", id, e);
                // Continue with empty list rather than failing completely
            }
        }

        EncounterDetailResponse response = new EncounterDetailResponse();
        response.setId(encounter.getId());
        
        // Safely access patient data - handle null patient case
        if (encounter.getPatient() != null) {
            log.debug("Encounter has patient association");
            try {
                response.setPatientId(encounter.getPatient().getId());
                String firstName = encounter.getPatient().getFirstName() != null ? encounter.getPatient().getFirstName() : "";
                String lastName = encounter.getPatient().getLastName() != null ? encounter.getPatient().getLastName() : "";
                response.setPatientName(firstName + " " + lastName);
                
                // Calculate age
                if (encounter.getPatient().getDateOfBirth() != null) {
                    response.setPatientAge(String.valueOf(java.time.Period.between(encounter.getPatient().getDateOfBirth(), LocalDate.now()).getYears()));
                }
                
                response.setPatientGender(encounter.getPatient().getGender());
                response.setMrnId(encounter.getPatient().getLocalMrnValue());
            } catch (Exception e) {
                log.error("Error accessing patient data for encounter ID: {}", id, e);
                // Set defaults and continue rather than failing
                response.setPatientId(null);
                response.setPatientName("Unknown Patient");
            }
        } else {
            log.warn("Encounter ID {} has no patient association", id);
            response.setPatientName("Service Provider Only");
        }
        
        response.setReferenceDoctor(encounter.getReferenceDoctor());
        response.setDate(encounter.getStartTime());
        response.setStatus(normalizeEncounterStatusForResponse(encounter.getStatus()));
        response.setLocalEncounterValue(encounter.getLocalEncounterValue());
        response.setTests(testNames);
        response.setServiceRequestIds(serviceRequests.stream().map(ServiceRequest::getId).collect(Collectors.toList()));
        response.setSpecimenBarcodes(specimenBarcodes);

        log.info("Successfully retrieved encounter details for ID: {}", id);
        return response;
    }

    @Transactional(readOnly = true)
    public PagedResponse<EncounterListResponse> getEncounters(Integer organizationId, Pageable pageable) {
        Page<Encounter> encounterPage = encounterRepository.findByPatient_Organization_Id(organizationId, pageable);
        return mapToPagedEncounterListResponse(encounterPage);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EncounterListResponse> searchEncounters(
            Integer organizationId,
            LocalDate startDate,
            LocalDate endDate,
            String patientName,
            String mrnId,
            Pageable pageable) {
            Specification<Encounter> specification = (root, query, cb) -> {
                List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

                predicates.add(cb.equal(root.get("patient").get("organization").get("id"), organizationId));

                if (startDate != null) {
                    OffsetDateTime start = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
                    predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), start));
                }

                if (endDate != null) {
                    OffsetDateTime end = endDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
                    predicates.add(cb.lessThanOrEqualTo(root.get("startTime"), end));
                }

                if (patientName != null && !patientName.isBlank()) {
                    String likeValue = "%" + patientName.toLowerCase() + "%";
                    predicates.add(cb.like(
                            cb.lower(cb.concat(cb.concat(root.get("patient").get("firstName"), " "), root.get("patient").get("lastName"))),
                            likeValue));
                }

                if (mrnId != null && !mrnId.isBlank()) {
                    predicates.add(cb.like(root.get("patient").get("localMrnValue"), "%" + mrnId + "%"));
                }

                return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
            };

            Page<Encounter> encounterPage = encounterRepository.findAll(specification, pageable);
        
        return mapToPagedEncounterListResponse(encounterPage);
    }

    // Backwards-compatible methods expected by controllers
    public EncounterResponse getEncounterById(Integer id) {
        return getEncounter(id);
    }

    public EncounterDetailResponse getEncounterDetailsById(Integer id) {
        return getEncounterDetail(id);
    }

    public List<EncounterResponse> getEncountersByPatient(Integer patientId) {
        List<Encounter> encounters = encounterRepository.findByPatient_Id(patientId);
        return encounters.stream().map(this::mapToEncounterResponse).collect(Collectors.toList());
    }

    public PagedResponse<EncounterListResponse> searchEncounters(
            Integer organizationId,
            LocalDate startDate,
            LocalDate endDate,
            List<Integer> testIds,
            String query,
            String department,
            String sampleCollector,
            String referringDoctor,
            String hospital,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        // For now map query to patientName and ignore other filters not implemented in repository
        return searchEncounters(organizationId, startDate, endDate, query, null, pageable);
    }

    private PagedResponse<EncounterListResponse> mapToPagedEncounterListResponse(Page<Encounter> encounterPage) {
        List<Encounter> encounters = encounterPage.getContent();
        
        // Bulk fetch tests for performance
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findByEncounterIn(encounters);
        List<ServiceRequestItem> items = serviceRequestItemRepository.findByServiceRequestIn(serviceRequests);
        
        Map<Integer, List<String>> encounterToTestsMap = items.stream()
            .filter(item -> item.getServiceRequest() != null && item.getServiceRequest().getEncounter() != null)
            .collect(Collectors.groupingBy(
                item -> item.getServiceRequest().getEncounter().getId(),
                Collectors.mapping(this::safeServiceRequestItemTestName, Collectors.toList())
            ));

        List<EncounterListResponse> content = encounters.stream().map(encounter -> {
            List<String> testNames = encounterToTestsMap.getOrDefault(encounter.getId(), Collections.emptyList())
                    .stream().distinct().collect(Collectors.toList());
            
            String status = encounter.getStatus();
            if (status != null) {
                status = EncounterStatus.fromCode(status).map(Enum::name).orElse(status);
            }
            
            return new EncounterListResponse(
                    encounter.getId(),
                    encounter.getPatient().getFirstName() + " " + encounter.getPatient().getLastName(),
                    encounter.getPatient().getLocalMrnValue(),
                    encounter.getReferenceDoctor(),
                    encounter.getStartTime(),
                    status,
                    testNames
            );
        }).collect(Collectors.toList());

        PagedResponse<EncounterListResponse> response = new PagedResponse<>();
        response.setContent(content);
        response.setPage(encounterPage.getNumber());
        response.setSize(encounterPage.getSize());
        response.setTotalElements(encounterPage.getTotalElements());
        response.setTotalPages(encounterPage.getTotalPages());

        return response;
    }

    private EncounterResponse mapToEncounterResponse(Encounter encounter) {
        EncounterResponse response = new EncounterResponse();
        response.setId(encounter.getId());
        response.setPatientId(encounter.getPatient().getId());
        response.setStartTime(encounter.getStartTime());
        response.setEndTime(encounter.getEndTime());
        response.setStatus(normalizeEncounterStatusForResponse(encounter.getStatus()));
        response.setEncounterClass(encounter.getEncounterClass());
        response.setServiceProviderId(encounter.getServiceProvider().getId());
        response.setLocalEncounterSystem(encounter.getLocalEncounterSystem());
        response.setLocalEncounterValue(encounter.getLocalEncounterValue());
        response.setReferenceDoctor(encounter.getReferenceDoctor());
        return response;
    }

    private String normalizeEncounterStatusCode(String status) {
        if (status == null) return null;
        return status.toLowerCase();
    }

    private String normalizeEncounterStatusForResponse(String status) {
        if (status == null) return null;
        return EncounterStatus.fromCode(status).map(Enum::name).orElse(status);
    }

    private String safeServiceRequestItemTestName(ServiceRequestItem item) {
        if (item == null) {
            return null;
        }
        if (item.getTest() != null && item.getTest().getTestName() != null && !item.getTest().getTestName().isBlank()) {
            return item.getTest().getTestName();
        }
        if (item.getPanel() != null && item.getPanel().getPanelName() != null && !item.getPanel().getPanelName().isBlank()) {
            return item.getPanel().getPanelName();
        }
        return "Unknown Test";
    }

    private Integer resolveEncounterOrganizationId(Encounter encounter) {
        if (encounter == null) {
            return null;
        }
        if (encounter.getServiceProvider() != null && encounter.getServiceProvider().getId() != null) {
            return encounter.getServiceProvider().getId();
        }
        if (encounter.getPatient() != null && encounter.getPatient().getOrganization() != null) {
            return encounter.getPatient().getOrganization().getId();
        }
        return null;
    }

    private Integer resolveOrganizationId(Organization organization, String entityName, Integer entityId) {
        if (organization == null || organization.getId() == null) {
            throw new AccessDeniedException("Cannot determine organization for " + entityName + " ID: " + entityId);
        }
        return organization.getId();
    }
}

package com.halo.lims.service;

import com.halo.lims.dto.PagedResponse;
import com.halo.lims.dto.encounter.EncounterCreateRequest;
import com.halo.lims.dto.encounter.EncounterListResponse;
import com.halo.lims.dto.encounter.EncounterResponse;
import com.halo.lims.dto.encounter.EncounterUpdateRequest;
import com.halo.lims.model.*;
import com.halo.lims.repository.*;
import com.halo.lims.security.SecurityService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EncounterService {

    private final EncounterRepository encounterRepository;
    private final PatientRepository patientRepository;
    private final OrganizationRepository organizationRepository;
    private final SecurityService securityService;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestItemRepository serviceRequestItemRepository;

    public EncounterService(EncounterRepository encounterRepository,
                            PatientRepository patientRepository,
                            OrganizationRepository organizationRepository,
                            SecurityService securityService, 
                            ServiceRequestRepository serviceRequestRepository,
                            ServiceRequestItemRepository serviceRequestItemRepository) {
        this.encounterRepository = encounterRepository;
        this.patientRepository = patientRepository;
        this.organizationRepository = organizationRepository;
        this.securityService = securityService;
        this.serviceRequestRepository = serviceRequestRepository;
        this.serviceRequestItemRepository = serviceRequestItemRepository;
    }

    @Transactional
    public EncounterResponse createEncounter(EncounterCreateRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + request.getPatientId()));
        Organization serviceProvider = organizationRepository.findById(request.getServiceProviderId())
                .orElseThrow(() -> new RuntimeException("Service Provider Organization not found with ID: " + request.getServiceProviderId()));

        // --- Multi-tenancy check ---
        Integer patientOrgId = patient.getOrganization().getId();
        if (!securityService.isUserInOrganization(patientOrgId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to create encounters for patients in organization ID: " + patientOrgId);
        }
        // Ensure the service provider is also within the user's accessible organizations (optional, but good practice)
        if (!securityService.isUserInOrganization(serviceProvider.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to create encounters with service provider organization ID: " + serviceProvider.getId());
        }
        // --- End multi-tenancy check ---

        Encounter encounter = Encounter.builder()
                .patient(patient)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(request.getStatus())
                .encounterClass(request.getEncounterClass())
                .serviceProvider(serviceProvider)
                .localEncounterSystem("http://com.lims/encounter-id")
                .localEncounterValue(generateLocalEncounterId())
                .referenceDoctor(request.getReferenceDoctor())
                .build();

        Encounter savedEncounter = encounterRepository.save(encounter);
        return mapToEncounterResponse(savedEncounter);
    }

    @Transactional
    public EncounterResponse updateEncounter(Integer id, EncounterUpdateRequest request) {
        Encounter encounter = encounterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encounter not found with ID: " + id));

        // --- Multi-tenancy check ---
        Integer patientOrgId = encounter.getPatient().getOrganization().getId();
        if (!securityService.isUserInOrganization(patientOrgId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to update encounters for patients in organization ID: " + patientOrgId);
        }
        // --- End multi-tenancy check ---

        if (request.getEndTime() != null) encounter.setEndTime(request.getEndTime());
        if (request.getStatus() != null) encounter.setStatus(request.getStatus());
        if (request.getEncounterClass() != null) encounter.setEncounterClass(request.getEncounterClass());

        Encounter updatedEncounter = encounterRepository.save(encounter);
        return mapToEncounterResponse(updatedEncounter);
    }

    @Transactional(readOnly = true)
    public EncounterResponse getEncounterById(Integer id) {
        Encounter encounter = encounterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encounter not found with ID: " + id));

        // --- Multi-tenancy check ---
        Integer patientOrgId = encounter.getPatient().getOrganization().getId();
        if (!securityService.isUserInOrganization(patientOrgId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to view encounters for patients in organization ID: " + patientOrgId);
        }
        // --- End multi-tenancy check ---

        return mapToEncounterResponse(encounter);
    }

    @Transactional(readOnly = true)
    public List<EncounterResponse> getEncountersByPatient(Integer patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));

        // --- Multi-tenancy check ---
        Integer patientOrgId = patient.getOrganization().getId();
        if (!securityService.isUserInOrganization(patientOrgId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to view encounters for patients in organization ID: " + patientOrgId);
        }
        // --- End multi-tenancy check ---

        return encounterRepository.findByPatient(patient).stream()
                .map(this::mapToEncounterResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<EncounterListResponse> searchEncounters(
            Integer organizationId, LocalDate startDate, LocalDate endDate, List<Integer> testIds, String query, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Specification<Encounter> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Encounter, Patient> patientJoin = root.join("patient");

            predicates.add(cb.equal(patientJoin.get("organization").get("id"), organizationId));

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), startDate.atStartOfDay().atOffset(ZoneOffset.UTC)));
            }
            if (endDate != null) {
                predicates.add(cb.lessThan(root.get("startTime"), endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC)));
            }

            if (StringUtils.isNotBlank(query)) {
                String likePattern = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(patientJoin.get("firstName")), likePattern),
                        cb.like(cb.lower(patientJoin.get("lastName")), likePattern),
                        cb.like(cb.lower(patientJoin.get("localMrnValue")), likePattern)
                ));
            }

            if (testIds != null && !testIds.isEmpty()) {
                Subquery<Integer> subquery = criteriaQuery.subquery(Integer.class);
                Root<ServiceRequestItem> itemRoot = subquery.from(ServiceRequestItem.class);
                Join<ServiceRequestItem, ServiceRequest> srJoin = itemRoot.join("serviceRequest");

                subquery.select(srJoin.get("encounter").get("id"))
                        .where(itemRoot.get("test").get("id").in(testIds));

                predicates.add(root.get("id").in(subquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Encounter> encounterPage = encounterRepository.findAll(spec, pageable);
        List<Encounter> encounters = encounterPage.getContent();

        if (encounters.isEmpty()) {
            return new PagedResponse<>(Collections.emptyList(), page, size, 0, 0);
        }

        List<ServiceRequest> serviceRequests = serviceRequestRepository.findByEncounterIn(encounters);
        List<ServiceRequestItem> serviceRequestItems = serviceRequestItemRepository.findByServiceRequestIn(serviceRequests);

        Map<Integer, List<String>> encounterToTestsMap = new HashMap<>();
        for (ServiceRequestItem item : serviceRequestItems) {
            if (item.getServiceRequest() != null && item.getServiceRequest().getEncounter() != null && item.getTest() != null) {
                encounterToTestsMap
                        .computeIfAbsent(item.getServiceRequest().getEncounter().getId(), k -> new ArrayList<>())
                        .add(item.getTest().getTestName());
            }
        }

        List<EncounterListResponse> content = encounters.stream().map(encounter -> {
            List<String> testNames = encounterToTestsMap.getOrDefault(encounter.getId(), Collections.emptyList())
                    .stream().distinct().collect(Collectors.toList());
            return new EncounterListResponse(
                    encounter.getId(),
                    encounter.getPatient().getFirstName() + " " + encounter.getPatient().getLastName(),
                    encounter.getPatient().getLocalMrnValue(),
                    encounter.getReferenceDoctor(),
                    encounter.getStartTime(),
                    encounter.getStatus(),
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

    private String generateLocalEncounterId() {
        return "ENC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private EncounterResponse mapToEncounterResponse(Encounter encounter) {
        EncounterResponse response = new EncounterResponse();
        response.setId(encounter.getId());
        response.setLocalEncounterValue(encounter.getLocalEncounterValue());
        response.setPatientId(encounter.getPatient().getId());
        response.setPatientMrn(encounter.getPatient().getLocalMrnValue());
        response.setPatientName(encounter.getPatient().getFirstName() + " " + encounter.getPatient().getLastName());
        response.setStartTime(encounter.getStartTime());
        response.setEndTime(encounter.getEndTime());
        response.setStatus(encounter.getStatus());
        response.setEncounterClass(encounter.getEncounterClass());
        if (encounter.getServiceProvider() != null) {
            response.setServiceProviderId(encounter.getServiceProvider().getId());
            response.setServiceProviderName(encounter.getServiceProvider().getOrganizationName());
        }
        response.setOrganizationId(encounter.getPatient().getOrganization().getId()); // Patient's organization
        response.setCreatedAt(encounter.getCreatedAt());
        response.setUpdatedAt(encounter.getUpdatedAt());
        response.setReferenceDoctor(encounter.getReferenceDoctor());
        return response;
    }
}

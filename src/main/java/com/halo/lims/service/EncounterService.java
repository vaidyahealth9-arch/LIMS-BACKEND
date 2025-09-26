package com.halo.lims.service;

import com.halo.lims.dto.encounter.EncounterCreateRequest;
import com.halo.lims.dto.encounter.EncounterResponse;
import com.halo.lims.dto.encounter.EncounterUpdateRequest;
import com.halo.lims.model.Encounter;
import com.halo.lims.model.Organization;
import com.halo.lims.model.Patient;
import com.halo.lims.repository.EncounterRepository;
import com.halo.lims.repository.OrganizationRepository;
import com.halo.lims.repository.PatientRepository;
import com.halo.lims.security.SecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EncounterService {

    private final EncounterRepository encounterRepository;
    private final PatientRepository patientRepository;
    private final OrganizationRepository organizationRepository;
    private final SecurityService securityService;

    public EncounterService(EncounterRepository encounterRepository,
                            PatientRepository patientRepository,
                            OrganizationRepository organizationRepository,
                            SecurityService securityService) {
        this.encounterRepository = encounterRepository;
        this.patientRepository = patientRepository;
        this.organizationRepository = organizationRepository;
        this.securityService = securityService;
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
        response.setServiceProviderId(encounter.getServiceProvider().getId());
        response.setServiceProviderName(encounter.getServiceProvider().getOrganizationName());
        response.setOrganizationId(encounter.getPatient().getOrganization().getId()); // Patient's organization
        response.setCreatedAt(encounter.getCreatedAt());
        response.setUpdatedAt(encounter.getUpdatedAt());
        return response;
    }
}

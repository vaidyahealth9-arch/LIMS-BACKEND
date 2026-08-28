package com.halo.lims.service;

import com.halo.lims.dto.PagedResponse;
import com.halo.lims.dto.patient.AbhaOtpVerificationRequest;
import com.halo.lims.dto.patient.PatientRegistrationRequest;
import com.halo.lims.dto.patient.PatientRegistrationResponse;
import com.halo.lims.model.Organization;
import com.halo.lims.model.Patient;
import com.halo.lims.repository.OrganizationRepository;
import com.halo.lims.repository.PatientRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final OrganizationRepository organizationRepository;
    private final PhrInternalClient phrInternalClient;

    public PatientService(PatientRepository patientRepository,
                          OrganizationRepository organizationRepository,
                          PhrInternalClient phrInternalClient) {
        this.patientRepository = patientRepository;
        this.organizationRepository = organizationRepository;
        this.phrInternalClient = phrInternalClient;
    }

    @Transactional
    public PatientRegistrationResponse registerPatient(PatientRegistrationRequest request) {
        // Validate organization exists
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + request.getOrganizationId()));

        Patient patient;
        boolean isNew = false;

        // 1. Check if we are updating an existing patient by ID
        if (request.getId() != null) {
            patient = patientRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + request.getId()));
            log.info("Updating existing patient ID: {} in organization: {}", patient.getId(), organization.getOrganizationName());
        } else {
            // 2. Deduplication Logic: Check if a patient with same name and phone exists in THIS organization
            // We only do this for non-dependents or if we want to prevent duplicates for dependents too.
            // For now, let's check by phone + firstName + lastName.
            List<Patient> existingMatches = patientRepository.findByOrganization(organization).stream()
                    .filter(p -> StringUtils.equalsIgnoreCase(p.getFirstName(), request.getFirstName()) &&
                                 StringUtils.equalsIgnoreCase(p.getLastName(), request.getLastName()) &&
                                 StringUtils.equals(Patient.normalizePhone(p.getContactPhone()), Patient.normalizePhone(request.getContactPhone())))
                    .collect(Collectors.toList());

            if (!existingMatches.isEmpty()) {
                patient = existingMatches.get(0);
                log.info("Found existing match for patient {} {} with phone {} in organization {}. Updating record.", 
                        request.getFirstName(), request.getLastName(), request.getContactPhone(), organization.getOrganizationName());
            } else {
                // 3. New Patient
                patient = new Patient();
                isNew = true;
                String localMrnValue = generateLocalMrn(organization.getLocalIdentifierValue());
                patient.setLocalMrnSystem("http://com.lims/patient-id/" + organization.getLocalIdentifierValue());
                patient.setLocalMrnValue(localMrnValue);
                patient.setAbdmLinkStatus("NOT_LINKED");
                patient.setOrganization(organization);
                log.info("Registering new patient: {} {} for organization: {}", request.getFirstName(), request.getLastName(), organization.getOrganizationName());
            }
        }

        // Map DTO to Entity
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setMiddleName(request.getMiddleName());
        patient.setGender(request.getGender());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setContactPhone(request.getContactPhone());
        patient.setContactEmail(request.getContactEmail());
        patient.setAddressLine1(request.getAddressLine1());
        patient.setAddressLine2(request.getAddressLine2());
        patient.setCity(request.getCity());
        patient.setState(request.getState());
        patient.setPostalCode(request.getPostalCode());
        patient.setCountry("IND");
        
        // Relationship and Dependent status
        patient.setRelationship(request.getRelationship() != null ? request.getRelationship() : "self");
        patient.setIsDependent(request.getIsDependent() != null ? request.getIsDependent() : false);

        // ABDM/ABHA integration is a future feature — not yet implemented.
        // If ABHA fields are submitted, log a warning and ignore them gracefully.
        if (StringUtils.isNotBlank(request.getAadhaarNumber())
                || StringUtils.isNotBlank(request.getAbhaLinkMobileNumber())
                || StringUtils.isNotBlank(request.getAbhaIdToLink())) {
            log.warn("ABHA linking fields submitted for patient {} {} but ABDM integration is not yet active. Fields ignored.",
                    request.getFirstName(), request.getLastName());
        }

        Patient savedPatient = patientRepository.save(patient);
        return mapToPatientRegistrationResponse(savedPatient);
    }

    @Transactional
    public PatientRegistrationResponse verifyAbhaAndLink(AbhaOtpVerificationRequest verificationRequest, Integer patientId) {
        // ABDM integration is a future feature. This endpoint is reserved but not yet active.
        throw new UnsupportedOperationException(
                "ABHA OTP verification is not yet implemented. ABDM integration is pending."
        );
    }


    private String generateLocalMrn(String organizationLocalId) {
        String datePart = java.time.OffsetDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        // TODO: Replace with an org-scoped sequence counter for truly unique MRNs per org.
        String suffix = String.format("%04d", (patientRepository.count() + 1));
        return organizationLocalId + "-" + datePart + "-" + suffix;
    }

    private PatientRegistrationResponse mapToPatientRegistrationResponse(Patient patient) {
        PatientRegistrationResponse response = new PatientRegistrationResponse();
        response.setId(patient.getId());
        response.setLocalMrnValue(patient.getLocalMrnValue());
        response.setFirstName(patient.getFirstName());
        response.setLastName(patient.getLastName());
        response.setGender(patient.getGender());
        response.setDateOfBirth(patient.getDateOfBirth());
        response.setAbhaId(patient.getAbhaId());
        response.setAbhaAddress(patient.getAbhaAddress());
        response.setAbdmLinkStatus(patient.getAbdmLinkStatus());
        response.setCreatedAt(patient.getCreatedAt());
        response.setContactPhone(patient.getContactPhone());
        response.setContactEmail(patient.getContactEmail());
        response.setAddressLine1(patient.getAddressLine1());
        response.setCity(patient.getCity());
        response.setState(patient.getState());
        response.setPostalCode(patient.getPostalCode());
        response.setRelationship(patient.getRelationship());
        response.setIsDependent(patient.getIsDependent());
        // Add organization ID to response
        response.setOrganizationId(patient.getOrganization().getId());
        return response;
    }

    // New method to find patients by organization (for fetching patients for a specific lab)
    @Transactional(readOnly = true)
    public List<PatientRegistrationResponse> getPatientsByOrganization(Integer organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));

        return patientRepository.findByOrganization(organization).stream()
                .map(this::mapToPatientRegistrationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PatientRegistrationResponse> findPatientByMobile(String mobile, String relationship) {
        String normalizedMobile = Patient.normalizePhone(mobile);
        if (normalizedMobile == null) {
            return Optional.empty();
        }

        String targetRelationship = (relationship != null && !relationship.isBlank()) ? relationship.toLowerCase() : "self";

        Optional<PatientRegistrationResponse> phrProfile = phrInternalClient.fetchPatientProfileByMobile(normalizedMobile, targetRelationship);
        if (phrProfile.isPresent()) {
            return phrProfile;
        }

        List<Patient> patients = patientRepository.findByContactPhoneNormalized(normalizedMobile);
        if (patients.isEmpty()) {
            patients = patientRepository.findAll().stream()
                    .filter(patient -> normalizedMobile.equals(Patient.normalizePhone(patient.getContactPhone())))
                    .collect(Collectors.toList());
        }

        if (patients.isEmpty()) {
            return Optional.empty();
        }

        // Filter by relationship if multiple patients share the mobile
        List<Patient> filteredPatients = patients.stream()
                .filter(p -> targetRelationship.equalsIgnoreCase(p.getRelationship()))
                .collect(Collectors.toList());

        Patient patient;
        if (!filteredPatients.isEmpty()) {
            patient = filteredPatients.stream()
                    .max(Comparator.comparing(Patient::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                    .orElse(filteredPatients.get(0));
        } else {
            // If no exact relationship match in LIMS, and it was "self", return the primary
            if ("self".equals(targetRelationship)) {
                patient = patients.stream()
                        .max(Comparator.comparing(Patient::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                        .orElse(patients.get(0));
            } else {
                return Optional.empty(); // Relationship requested but not found
            }
        }

        return Optional.of(mapToPatientRegistrationResponse(patient));
    }

    @Transactional(readOnly = true)
    public Optional<PatientRegistrationResponse> findPatientByAccessCodeAndMobile(String code, String mobile) {
        if (code == null || code.isBlank() || mobile == null || mobile.isBlank()) {
            return Optional.empty();
        }
        return phrInternalClient.fetchPatientProfileByAccessCode(code, mobile);
    }


    // Backwards-compatible overload used by tests and older callers
    @Transactional(readOnly = true)
    public Optional<PatientRegistrationResponse> findPatientByMobile(String mobile) {
        return findPatientByMobile(mobile, null);
    }

    // New method for searching patients within an organization
    @Transactional(readOnly = true)
    public PagedResponse<PatientRegistrationResponse> searchPatientsInOrganization(
            Integer organizationId, String query, int page, int size) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));

        Pageable pageable = PageRequest.of(page, size);
        query = query.trim();
        Page<Patient> patientPage = patientRepository.searchPatients(organization, query, pageable);

        List<PatientRegistrationResponse> content = patientPage.getContent().stream()
                .map(this::mapToPatientRegistrationResponse)
                .collect(Collectors.toList());

        PagedResponse<PatientRegistrationResponse> response = new PagedResponse<>();
        response.setContent(content);
        response.setPage(patientPage.getNumber());
        response.setSize(patientPage.getSize());
        response.setTotalElements(patientPage.getTotalElements());
        response.setTotalPages(patientPage.getTotalPages());

        return response;
    }

}

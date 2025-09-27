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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final OrganizationRepository organizationRepository; // Inject OrganizationRepository
    private final AbdmService abdmService;

    public PatientService(PatientRepository patientRepository,
                          OrganizationRepository organizationRepository, // Inject
                          AbdmService abdmService) {
        this.patientRepository = patientRepository;
        this.organizationRepository = organizationRepository; // Assign
        this.abdmService = abdmService;
    }

    @Transactional
    public PatientRegistrationResponse registerPatient(PatientRegistrationRequest request) {
        // Validate organization exists
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + request.getOrganizationId()));

        // 1. Generate local MRN
        // Consider making MRN generation organization-specific: LAB<ORG_CODE>YYMMDDXXXX
        String localMrnValue = generateLocalMrn(organization.getLocalIdentifierValue());

        // 2. Check for existing patient within THIS organization
        // This is crucial for multi-tenancy. A patient with same ABHA/MRN might exist
        // at a different lab, but this is a new patient for *this* organization.
        Optional<Patient> existingPatient = patientRepository.findByLocalMrnValueAndOrganization(localMrnValue, organization);
        // OR: If ABHA is unique across the whole LIMS platform (not just per organization)
        // Optional<Patient> existingPatientByAbha = patientRepository.findByAbhaId(request.getAbhaIdToLink());

        Patient patient;
        if (existingPatient.isPresent()) {
            patient = existingPatient.get();
            // TODO: Update existing patient details if necessary.
            System.out.println("Found existing patient in organization " + organization.getOrganizationName() + ", updating...");
        } else {
            patient = new Patient();
            patient.setLocalMrnSystem("http://com.lims/patient-id/" + organization.getLocalIdentifierValue()); // Make system URI organization-specific
            patient.setLocalMrnValue(localMrnValue);
            patient.setAbdmLinkStatus("NOT_LINKED");
            patient.setOrganization(organization); // Link patient to the organization
            System.out.println("Creating new patient for organization " + organization.getOrganizationName() + "...");
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

        // 3. Handle ABHA creation/linking requests (Milestone 1 & 2)
        if (StringUtils.isNotBlank(request.getAadhaarNumber())|| StringUtils.isNotBlank(request.getAbhaLinkMobileNumber()) || StringUtils.isNotBlank(request.getAbhaIdToLink())) {
            String authMethod;
            if (request.getAadhaarNumber() != null) {
                authMethod = "AADHAAR_OTP";
                throw new UnsupportedOperationException("Aadhaar based ABHA creation/linking not yet implemented.");
            } else if (request.getAbhaLinkMobileNumber() != null) {
                authMethod = "MOBILE_OTP";
                String txnId = abdmService.initiateAbhaVerification(request, authMethod);
                patient.setAbdmLinkStatus("PENDING_OTP");
                patient.setAbdmStatusMessage("OTP sent to mobile for ABHA linking. Txn ID: " + txnId);
                System.out.println("ABHA linking initiated. TxnId: " + txnId);
            } else if (request.getAbhaIdToLink() != null) {
                authMethod = "HEALTH_ID";
                String txnId = abdmService.initiateAbhaVerification(request, authMethod);
                patient.setAbdmLinkStatus("PENDING_OTP");
                patient.setAbdmStatusMessage("OTP sent to ABHA registered mobile for linking. Txn ID: " + txnId);
                System.out.println("ABHA linking initiated for existing ID. TxnId: " + txnId);
            }
        }

        Patient savedPatient = patientRepository.save(patient);

        return mapToPatientRegistrationResponse(savedPatient);
    }

    @Transactional
    public PatientRegistrationResponse verifyAbhaAndLink(AbhaOtpVerificationRequest verificationRequest, Integer patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));

        // Security check: ensure the user making this call is authorized for this patient's organization
        // (This would be handled by Spring Security context and authorization logic)

        if (!"PENDING_OTP".equals(patient.getAbdmLinkStatus())) {
            throw new IllegalStateException("ABHA linking is not in PENDING_OTP status for patient: " + patientId);
        }

        PatientRegistrationRequest originalPatientData = new PatientRegistrationRequest();
        originalPatientData.setFirstName(patient.getFirstName());
        originalPatientData.setLastName(patient.getLastName());
        originalPatientData.setGender(patient.getGender());
        originalPatientData.setDateOfBirth(patient.getDateOfBirth());


        AbdmService.AbhaDetails abhaDetails = abdmService.confirmAbhaVerification(verificationRequest, originalPatientData);

        patient.setAbhaId(abhaDetails.getAbhaId());
        patient.setAbhaAddress(abhaDetails.getAbhaAddress());
        patient.setAbhaIdSystem("https://healthid.ndhm.gov.in");
        patient.setAbdmLinkStatus("LINKED");
        patient.setAbdmStatusMessage(null);
        patient.setAbdmLastLinkedAt(OffsetDateTime.now());

        Patient updatedPatient = patientRepository.save(patient);
        return mapToPatientRegistrationResponse(updatedPatient);
    }


    private String generateLocalMrn(String organizationLocalId) {
        String datePart = OffsetDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        // Need a robust, organization-specific sequence for MRNs in a real system.
        // For example: query `patientRepository.countByOrganization(organization)` and increment.
        String suffix = String.format("%04d", (patientRepository.count() + 1)); // Simple global counter for now
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

    // New method for searching patients within an organization
    @Transactional(readOnly = true)
    public PagedResponse<PatientRegistrationResponse> searchPatientsInOrganization(
            Integer organizationId, String query, int page, int size) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));

        Pageable pageable = PageRequest.of(page, size);
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

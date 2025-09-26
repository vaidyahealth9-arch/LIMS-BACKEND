package com.halo.lims.security;

import com.halo.lims.model.*;
import com.halo.lims.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides custom security logic for Spring Security's @PreAuthorize expressions.
 * This class should be registered as a Spring Bean with the name "securityService".
 */
@Service("securityService")
public class SecurityService {

    private final PatientRepository patientRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ObservationRepository observationRepository;
    private final EncounterRepository encounterRepository;
    private final SpecimenRepository specimenRepository;
    private final BillRepository billRepository;

    public SecurityService(PatientRepository patientRepository, ServiceRequestRepository serviceRequestRepository, ObservationRepository observationRepository, EncounterRepository encounterRepository, SpecimenRepository specimenRepository, BillRepository billRepository) {
        this.patientRepository = patientRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.observationRepository = observationRepository;
        this.encounterRepository = encounterRepository;
        this.specimenRepository = specimenRepository;
        this.billRepository = billRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            return null; // Or throw AccessDeniedException
        }
        return (User) authentication.getPrincipal();
    }


    /**
     * Checks if the currently authenticated user belongs to the specified organization.
     * An ADMIN user can access any organization's data.
     * @param organizationId The ID of the organization being accessed.
     * @return true if the user belongs to the organization or is an ADMIN, false otherwise.
     */
    public boolean isUserInOrganization(Integer organizationId) {
        // 1. Get the current authentication object from the SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If no user is authenticated or authentication is anonymous/unsupported
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            return false;
        }

        // 2. Get the authenticated user (our custom User entity)
        User currentUser = (User) authentication.getPrincipal();

        // 3. If the user has an 'ADMIN' role, they can access any organization
        if (currentUser.getRoles() != null && currentUser.getRoles().contains("ADMIN")) {
            return true;
        }

        // 4. Otherwise, check if the user's assigned organization matches the requested organization
        return currentUser.getOrganization() != null && currentUser.getOrganization().getId().equals(organizationId);
    }

    /**
     * Checks if the currently authenticated user can access the patient identified by patientId.
     * @param patientId The ID of the patient.
     * @return true if access is allowed, false otherwise.
     */
    public boolean canAccessPatient(Integer patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElse(null);

        if (patient == null) return false; // Patient not found

        return isUserInOrganization(patient.getOrganization().getId());
    }

    /**
     * Checks if the currently authenticated user can access the service request identified by serviceRequestId.
     * @param serviceRequestId The ID of the service request.
     * @return true if access is allowed, false otherwise.
     */
    public boolean canAccessServiceRequest(Integer serviceRequestId) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId)
                .orElse(null);

        if (serviceRequest == null) return false;

        return isUserInOrganization(serviceRequest.getPatient().getOrganization().getId());
    }

    /**
     * Checks if the currently authenticated user can access the observation identified by observationId.
     * @param observationId The ID of the observation.
     * @return true if access is allowed, false otherwise.
     */
    public boolean canAccessObservation(Integer observationId) {
        Observation observation = observationRepository.findById(observationId)
                .orElse(null);

        if (observation == null) return false; // Observation not found

        return isUserInOrganization(observation.getPatient().getOrganization().getId());
    }

    /**
     * Checks if the currently authenticated user can access all observations in a given batch.
     * All observations must belong to the same organization, and the user must have access to that organization.
     * @param observationIds A list of observation IDs.
     * @return true if access is allowed for all, false otherwise.
     */
    public boolean canAccessObservationsInBatch(List<Integer> observationIds) {
        if (observationIds == null || observationIds.isEmpty()) return true;

        Integer firstOrgId = null;
        for (Integer obsId : observationIds) {
            Observation observation = observationRepository.findById(obsId)
                    .orElse(null);
            if (observation == null) return false;

            Integer currentOrgId = observation.getPatient().getOrganization().getId();
            if (firstOrgId == null) {
                firstOrgId = currentOrgId;
            } else if (!firstOrgId.equals(currentOrgId)) {
                // Batch contains observations from different organizations
                return false;
            }
        }
        return isUserInOrganization(firstOrgId);
    }

    public boolean canAccessEncounter(Integer encounterId) {
        Encounter encounter = encounterRepository.findById(encounterId).orElse(null);
        if (encounter == null) return false;
        return isUserInOrganization(encounter.getPatient().getOrganization().getId());
    }

    public boolean canAccessSpecimen(Integer specimenId) {
        Specimen specimen = specimenRepository.findById(specimenId).orElse(null);
        if (specimen == null) return false;
        return isUserInOrganization(specimen.getPatient().getOrganization().getId());
    }

    public boolean canAccessBill(Integer billId) {
        Bill bill = billRepository.findById(billId).orElse(null);
        if (bill == null) return false;
        return isUserInOrganization(bill.getOrganization().getId());
    }
}

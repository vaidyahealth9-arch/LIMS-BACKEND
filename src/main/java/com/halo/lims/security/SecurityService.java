package com.halo.lims.security;

import com.halo.lims.model.*;
import com.halo.lims.repository.*;
import org.springframework.security.access.AccessDeniedException;
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

    public User getAuthenticatedUser() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Access denied: no authenticated user context");
        }
        return currentUser;
    }

    public boolean isCurrentUserInOrganizationStrict(Integer organizationId) {
        if (organizationId == null) {
            return false;
        }

        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getOrganization() == null) {
            return false;
        }

        return organizationId.equals(currentUser.getOrganization().getId());
    }


    /**
     * Checks if the currently authenticated user belongs to the specified organization.
     * @param organizationId The ID of the organization being accessed.
     * @return true only when the user belongs to the same organization.
     */
    public boolean isUserInOrganization(Integer organizationId) {
        return isCurrentUserInOrganizationStrict(organizationId);
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

    public boolean canAccessPatientStrict(Integer patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElse(null);

        if (patient == null || patient.getOrganization() == null) {
            return false;
        }

        return isCurrentUserInOrganizationStrict(patient.getOrganization().getId());
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

        if (serviceRequest.getEncounter() != null && serviceRequest.getEncounter().getServiceProvider() != null) {
            return isUserInOrganization(serviceRequest.getEncounter().getServiceProvider().getId());
        }

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

        if (encounter.getServiceProvider() != null && encounter.getServiceProvider().getId() != null) {
            return isUserInOrganization(encounter.getServiceProvider().getId());
        }

        return encounter.getPatient() != null
                && encounter.getPatient().getOrganization() != null
                && isUserInOrganization(encounter.getPatient().getOrganization().getId());
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

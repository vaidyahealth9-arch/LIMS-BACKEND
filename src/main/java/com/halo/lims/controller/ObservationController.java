package com.halo.lims.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.halo.lims.dto.observation.ObservationCreateRequest;
import com.halo.lims.dto.observation.ObservationResponse;
import com.halo.lims.dto.observation.ObservationUpdateRequest;
import com.halo.lims.security.CustomUserDetailsService;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.ObservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/observations")
public class ObservationController {

    private final ObservationService observationService;
    private final CustomUserDetailsService customUserDetailsService;
    private final SecurityService securityService;

    public ObservationController(ObservationService observationService, CustomUserDetailsService customUserDetailsService, SecurityService securityService) {
        this.observationService = observationService;
        this.customUserDetailsService = customUserDetailsService;
        this.securityService = securityService;
    }

    /**
     * Technician enters a new observation result.
     * Requires ROLE_TECHNICIAN.
     * @param request DTO for creating an observation.
     * @param userDetails Authenticated user details.
     * @return Created ObservationResponse.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN') and @securityService.canAccessServiceRequest(#request.serviceRequestId)")
    public ResponseEntity<ObservationResponse> createObservation(
            @Valid @RequestBody ObservationCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) { // Assuming UserDetails contains practitionerId
        // In a real application, you'd retrieve the Practitioner ID from userDetails
        // For now, let's mock it or assume userDetails.getUsername() corresponds to a Practitioner's local_identifier_value
        // and fetch the Practitioner ID using practitionerRepository.
        Integer performerId = customUserDetailsService.getPractitionerIdFromUserDetails(userDetails); // Implement this helper

        ObservationResponse response = observationService.createObservation(request, performerId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Technician updates an existing observation result.
     * Requires ROLE_TECHNICIAN.
     * @param id Observation ID.
     * @param request DTO for updating an observation.
     * @param userDetails Authenticated user details.
     * @return Updated ObservationResponse.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN') and @securityService.canAccessObservation(#id)")
    public ResponseEntity<ObservationResponse> updateObservation(
            @PathVariable Integer id,
            @Valid @RequestBody ObservationUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer performerId = customUserDetailsService.getPractitionerIdFromUserDetails(userDetails); // Implement this helper
        ObservationResponse response = observationService.updateObservation(id, request, performerId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Technician sends a batch of observations for verification.
     * Requires ROLE_TECHNICIAN.
     * @param observationIds List of Observation IDs.
     * @param userDetails Authenticated user details.
     * @return List of updated ObservationResponses.
     */
    @PostMapping("/send-for-verification")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN') and @securityService.canAccessObservationsInBatch(#observationIds)")
    public ResponseEntity<List<ObservationResponse>> sendObservationsForVerification(
            @RequestBody List<Integer> observationIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer technicianId = customUserDetailsService.getPractitionerIdFromUserDetails(userDetails);
        List<ObservationResponse> responses = observationService.sendForVerification(observationIds, technicianId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Pathologist approves a batch of observations, finalizing them.
     * Requires ROLE_PATHOLOGIST.
     * @param observationIds List of Observation IDs.
     * @param userDetails Authenticated user details.
     * @return List of finalized ObservationResponses.
     */
    @PostMapping("/approve")
    @PreAuthorize("hasAnyRole('PATHOLOGIST', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<List<ObservationResponse>> approveObservations(
            @RequestBody JsonNode requestBody,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Integer> observationIds = extractObservationIds(requestBody);
        if (!securityService.canAccessObservationsInBatch(observationIds)) {
            throw new AccessDeniedException("User not authorized to approve these observations");
        }

        Integer approvingPractitionerId = extractApprovingPractitionerId(requestBody);
        boolean isAdmin = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> "ROLE_ADMIN".equalsIgnoreCase(authority) || "ADMIN".equalsIgnoreCase(authority));

        if (approvingPractitionerId == null) {
            if (!isAdmin) {
                approvingPractitionerId = customUserDetailsService.getPractitionerIdFromUserDetails(userDetails);
            } else {
                approvingPractitionerId = customUserDetailsService.getPractitionerIdFromUserDetails(userDetails);
            }
        }

        List<ObservationResponse> responses = observationService.approveObservations(observationIds, approvingPractitionerId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    private List<Integer> extractObservationIds(JsonNode requestBody) {
        List<Integer> observationIds = new ArrayList<>();

        JsonNode idsNode = requestBody.isArray() ? requestBody : requestBody.get("observationIds");
        if (idsNode != null && idsNode.isArray()) {
            idsNode.forEach(node -> {
                if (node.isNumber()) {
                    observationIds.add(node.asInt());
                } else if (node.isTextual()) {
                    try {
                        observationIds.add(Integer.parseInt(node.asText()));
                    } catch (NumberFormatException ignored) {
                        // ignore malformed entries
                    }
                }
            });
        }

        if (observationIds.isEmpty()) {
            throw new IllegalArgumentException("Observation IDs are required for approval.");
        }

        return observationIds;
    }

    private Integer extractApprovingPractitionerId(JsonNode requestBody) {
        JsonNode practitionerIdNode = requestBody.isObject() ? requestBody.get("approvingPractitionerId") : null;
        if (practitionerIdNode == null || practitionerIdNode.isNull()) {
            return null;
        }
        if (practitionerIdNode.isNumber()) {
            return practitionerIdNode.asInt();
        }
        if (practitionerIdNode.isTextual()) {
            try {
                return Integer.parseInt(practitionerIdNode.asText());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * Retrieves all observations for a given service request.
     * @param serviceRequestId The ID of the service request.
     * @return List of ObservationResponses.
     */
    @GetMapping("/by-service-request/{serviceRequestId}")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'PATHOLOGIST', 'DOCTOR') and @securityService.canAccessServiceRequest(#serviceRequestId)")
    public ResponseEntity<List<ObservationResponse>> getObservationsByServiceRequestId(@PathVariable Integer serviceRequestId) {
        List<ObservationResponse> responses = observationService.getObservationsByServiceRequestId(serviceRequestId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Retrieves a single observation by its ID.
     * @param id Observation ID.
     * @return ObservationResponse.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'PATHOLOGIST', 'DOCTOR') and @securityService.canAccessObservation(#id)")
    public ResponseEntity<ObservationResponse> getObservationById(@PathVariable Integer id) {
        ObservationResponse response = observationService.getObservationById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

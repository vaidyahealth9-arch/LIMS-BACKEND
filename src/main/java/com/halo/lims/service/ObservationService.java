package com.halo.lims.service;

import com.halo.lims.dto.observation.ObservationCreateRequest;
import com.halo.lims.dto.observation.ObservationResponse;
import com.halo.lims.dto.observation.ObservationUpdateRequest;
import com.halo.lims.model.*;
import com.halo.lims.repository.*;
import com.halo.lims.security.SecurityService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ObservationService {

    private final ObservationRepository observationRepository;
    private final OrganizationAnalyteInterpretationRuleRepository organizationAnalyteInterpretationRuleRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final SpecimenRepository specimenRepository;
    private final TestAnalyteRepository testAnalyteRepository;
    private final ReferenceRangeRepository referenceRangeRepository;
    private final TestInterpretationRuleRepository testInterpretationRuleRepository;
    private final PractitionerRepository practitionerRepository;
    private final SecurityService securityService;

    public ObservationService(
            ObservationRepository observationRepository,
            OrganizationAnalyteInterpretationRuleRepository organizationAnalyteInterpretationRuleRepository,
            ServiceRequestRepository serviceRequestRepository,
            SpecimenRepository specimenRepository,
            TestAnalyteRepository testAnalyteRepository,
            ReferenceRangeRepository referenceRangeRepository,
            TestInterpretationRuleRepository testInterpretationRuleRepository,
            PractitionerRepository practitionerRepository, SecurityService securityService) {
        this.observationRepository = observationRepository;
        this.organizationAnalyteInterpretationRuleRepository = organizationAnalyteInterpretationRuleRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.specimenRepository = specimenRepository;
        this.testAnalyteRepository = testAnalyteRepository;
        this.referenceRangeRepository = referenceRangeRepository;
        this.testInterpretationRuleRepository = testInterpretationRuleRepository;
        this.practitionerRepository = practitionerRepository;
        this.securityService = securityService;
    }

    /**
     * Creates a new Observation record.
     * This is typically performed by a Technician.
     * @param request The DTO containing observation details.
     * @param performerId The ID of the Practitioner (technician) performing the action.
     * @return The created ObservationResponse.
     */
    @Transactional
    public ObservationResponse createObservation(ObservationCreateRequest request, Integer performerId) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(request.getServiceRequestId())
                .orElseThrow(() -> new RuntimeException("Service Request not found with ID: " + request.getServiceRequestId()));
        Specimen specimen = null;
        if(Objects.nonNull(request.getSpecimenId())){
            specimen = specimenRepository.findById(request.getSpecimenId())
                    .orElseThrow(() -> new RuntimeException("Specimen not found with ID: " + request.getSpecimenId()));
        }
        TestAnalyte analyte = testAnalyteRepository.findById(request.getAnalyteId())
                .orElseThrow(() -> new RuntimeException("Analyte not found with ID: " + request.getAnalyteId()));
        Practitioner performer = practitionerRepository.findById(performerId)
                .orElseThrow(() -> new RuntimeException("Performer not found with ID: " + performerId));

        // --- Multi-tenancy check (internal) ---
        Integer organizationId = serviceRequest.getPatient().getOrganization().getId();
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new AccessDeniedException("User not authorized to create observations for organization ID: " + organizationId);
        }
        // --- End multi-tenancy check ---

        if (Objects.nonNull(specimen) && !serviceRequest.getPatient().getId().equals(specimen.getPatient().getId())) {
            throw new IllegalArgumentException("Patient mismatch between Service Request and Specimen.");
        }

        Observation observation = new Observation();
        observation.setServiceRequest(serviceRequest);
        observation.setSpecimen(specimen);
        observation.setAnalyte(analyte);
        observation.setPatient(serviceRequest.getPatient()); // Use patient from ServiceRequest

        // Set value based on analyte type
        switch (analyte.getResultType().toLowerCase()) {
            case "numeric":
                if (request.getValueNumeric() == null) throw new IllegalArgumentException("Numeric value required for this analyte.");
                observation.setValueNumeric(request.getValueNumeric());
                break;
            case "text":
                if (request.getValueString() == null) throw new IllegalArgumentException("String value required for this analyte.");
                observation.setValueString(request.getValueString());
                break;
            case "coded":
                if (request.getValueCode() == null) throw new IllegalArgumentException("Coded value required for this analyte.");
                observation.setValueCode(request.getValueCode());
                observation.setValueCodeSystem(request.getValueCodeSystem() != null ? request.getValueCodeSystem() : "http://lims.com/codesystem/local"); // Default for local codes
                break;
            default:
                throw new IllegalArgumentException("Unsupported result type for analyte: " + analyte.getResultType());
        }

        observation.setUnit(analyte.getUnit()); // Default unit from analyte definition
        observation.setEffectiveDateTime(request.getEffectiveDateTime() != null ? request.getEffectiveDateTime() : OffsetDateTime.now());
        observation.setIssuedDateTime(OffsetDateTime.now()); // Initially set, will be updated on finalization
        observation.setStatus("preliminary"); // Technician enters as preliminary
        observation.setPerformer(performer);

        // Generate a unique local observation ID
        observation.setLocalObservationSystem("http://com.lims/observation-id");
        observation.setLocalObservationValue(generateLocalObservationId()); // e.g., OBS+SRID+ANID+YYMMDD

        // Basic interpretation based on reference ranges
        applyReferenceRangeInterpretation(observation, analyte);

        Observation savedObservation = observationRepository.save(observation);
        return mapToObservationResponse(savedObservation);
    }

    /**
     * Updates an existing Observation record.
     * Can be done by Technician or Pathologist.
     * @param id The ID of the Observation to update.
     * @param request The DTO containing updated observation details.
     * @param performerId The ID of the Practitioner (technician/pathologist) performing the action.
     * @return The updated ObservationResponse.
     */
    @Transactional
    public ObservationResponse updateObservation(Integer id, ObservationUpdateRequest request, Integer performerId) {
        Observation observation = observationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Observation not found with ID: " + id));
        Practitioner performer = practitionerRepository.findById(performerId)
                .orElseThrow(() -> new RuntimeException("Performer not found with ID: " + performerId));

        // --- Multi-tenancy check (internal) ---
        Integer organizationId = observation.getPatient().getOrganization().getId();
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new AccessDeniedException("User not authorized to update observations for organization ID: " + organizationId);
        }
        // --- End multi-tenancy check ---

        if ("final".equals(observation.getStatus()) || "cancelled".equals(observation.getStatus())) {
            throw new IllegalStateException("Cannot update a finalized or cancelled observation.");
        }

        TestAnalyte analyte = observation.getAnalyte();

        // Update value based on analyte type
        switch (analyte.getResultType().toLowerCase()) {
            case "numeric":
                if (request.getValueNumeric() == null) throw new IllegalArgumentException("Numeric value required for this analyte.");
                observation.setValueNumeric(request.getValueNumeric());
                observation.setValueString(null); observation.setValueCode(null);
                break;
            case "text":
                if (request.getValueString() == null) throw new IllegalArgumentException("String value required for this analyte.");
                observation.setValueString(request.getValueString());
                observation.setValueNumeric(null); observation.setValueCode(null);
                break;
            case "coded":
                if (request.getValueCode() == null) throw new IllegalArgumentException("Coded value required for this analyte.");
                observation.setValueCode(request.getValueCode());
                observation.setValueCodeSystem(request.getValueCodeSystem() != null ? request.getValueCodeSystem() : "http://lims.com/codesystem/local");
                observation.setValueNumeric(null); observation.setValueString(null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported result type for analyte: " + analyte.getResultType());
        }

        observation.setEffectiveDateTime(request.getEffectiveDateTime() != null ? request.getEffectiveDateTime() : OffsetDateTime.now());
        observation.setPerformer(performer); // Update performer if changed

        // Basic interpretation based on reference ranges
        applyReferenceRangeInterpretation(observation, analyte);

        Observation updatedObservation = observationRepository.save(observation);
        return mapToObservationResponse(updatedObservation);
    }

    /**
     * Sends a batch of observations for verification.
     * @param observationIds List of Observation IDs to send for verification.
     * @param technicianId The ID of the Technician performing the action.
     * @return List of updated ObservationResponses.
     */
    @Transactional
    public List<ObservationResponse> sendForVerification(List<Integer> observationIds, Integer technicianId) {
        List<Observation> observations = observationRepository.findAllById(observationIds);
        if (observations.isEmpty()) {
            throw new RuntimeException("No observations found for verification.");
        }

        // --- Multi-tenancy check for ALL observations in the batch ---
        // Ensure all observations belong to the same organization and user has access
        Integer organizationId = observations.get(0).getPatient().getOrganization().getId(); // Assuming all belong to same org
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new AccessDeniedException("User not authorized to verify observations for organization ID: " + organizationId);
        }
        // You might want to loop and check each observation's organization for robust check
        for (Observation obs : observations) {
            if (!obs.getPatient().getOrganization().getId().equals(organizationId)) {
                throw new IllegalArgumentException("Batch verification contains observations from multiple organizations.");
            }
        }
        // --- End multi-tenancy check ---

        // Validate that the technician is authorized (via Spring Security in controller)
        // And that observations are in a state that can be sent for verification
        observations.forEach(obs -> {
            if (!"preliminary".equals(obs.getStatus()) && !"amended".equals(obs.getStatus())) {
                throw new IllegalStateException("Observation " + obs.getLocalObservationValue() + " cannot be sent for verification as its status is " + obs.getStatus());
            }
            obs.setStatus("pending-verification"); // Custom status for the workflow
            obs.setPerformer(practitionerRepository.findById(technicianId) // Optionally update performer here
                    .orElseThrow(() -> new RuntimeException("Technician not found")));
        });

        List<Observation> updatedObservations = observationRepository.saveAll(observations);
        return updatedObservations.stream()
                .map(this::mapToObservationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Approves and finalizes a batch of observations.
     * This is typically performed by a Pathologist.
     * @param observationIds List of Observation IDs to approve.
     * @param pathologistId The ID of the Practitioner (pathologist) performing the action.
     * @return List of finalized ObservationResponses.
     */
    @Transactional
    public List<ObservationResponse> approveObservations(List<Integer> observationIds, Integer pathologistId) {
        List<Observation> observations = observationRepository.findAllById(observationIds);
        if (observations.isEmpty()) {
            throw new RuntimeException("No observations found for approval.");
        }

        // --- Multi-tenancy check for ALL observations in the batch ---
        Integer organizationId = observations.get(0).getPatient().getOrganization().getId(); // Assuming all belong to same org
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new AccessDeniedException("User not authorized to approve observations for organization ID: " + organizationId);
        }
        for (Observation obs : observations) {
            if (!obs.getPatient().getOrganization().getId().equals(organizationId)) {
                throw new IllegalArgumentException("Batch approval contains observations from multiple organizations.");
            }
        }
        // --- End multi-tenancy check ---

        // Validate that the pathologist is authorized and observations are ready for approval
        Practitioner pathologist = practitionerRepository.findById(pathologistId)
                .orElseThrow(() -> new RuntimeException("Pathologist not found with ID: " + pathologistId));

        observations.forEach(obs -> {
            if (!"pending-verification".equals(obs.getStatus()) && !"preliminary".equals(obs.getStatus())) {
                throw new IllegalStateException("Observation " + obs.getLocalObservationValue() + " cannot be approved as its status is " + obs.getStatus());
            }
            obs.setStatus("final"); // FHIR status for final
            obs.setIssuedDateTime(OffsetDateTime.now()); // Time of finalization
            obs.setPerformer(pathologist); // Pathologist as the final verifier
            // Apply advanced interpretation rules if any (Milestone 7.6)
            applyInterpretationRules(obs);
        });

        List<Observation> approvedObservations = observationRepository.saveAll(observations);

        // TODO: Trigger DiagnosticReport creation/update if all observations for a service request are final
        // For example: check if all ServiceRequestItems for the SR are finalized, then generate DiagnosticReport.

        return approvedObservations.stream()
                .map(this::mapToObservationResponse)
                .collect(Collectors.toList());
    }


    /**
     * Finds and applies the most specific reference range interpretation.
     * @param observation The observation to interpret.
     * @param analyte The analyte definition.
     */
    private void applyReferenceRangeInterpretation(Observation observation, TestAnalyte analyte) {
        // This is a simplified logic. A full implementation would consider patient's age/gender.
        List<ReferenceRange> ranges = referenceRangeRepository.findByAnalyte(analyte);

        if (observation.getValueNumeric() != null) {
            BigDecimal resultValue = observation.getValueNumeric();
            ReferenceRange matchingRange = ranges.stream()
                    // Filter by gender and age if available and applicable (for now, simple filter or first match)
                    .filter(range -> (range.getGender() == null || range.getGender().equalsIgnoreCase(observation.getPatient().getGender())))
                    // Sort to find most specific, or just take first applicable
                    .min(Comparator.comparing(rr -> (rr.getMinAgeYears() != null ? rr.getMinAgeYears() : -1) + (rr.getMaxAgeYears() != null ? rr.getMaxAgeYears() : 999))) // Simple attempt to prefer more specific ranges
                    .orElse(null);

            if (matchingRange != null) {
                if (resultValue.compareTo(matchingRange.getLowValue()) < 0) {
                    observation.setInterpretationCode("L"); // Low
                } else if (resultValue.compareTo(matchingRange.getHighValue()) > 0) {
                    observation.setInterpretationCode("H"); // High
                } else {
                    observation.setInterpretationCode("N"); // Normal
                }
                observation.setReferenceRange(matchingRange);
            } else {
                observation.setInterpretationCode("UNK"); // Unknown/No Range
            }
        } else if (observation.getValueString() != null || observation.getValueCode() != null) {
            // For text/coded results, direct mapping or specific rules needed
            // e.g., if valueString "Negative" maps to "N"
            ReferenceRange matchingRange = ranges.stream()
                    .filter(range -> range.getTextRange() != null &&
                            (range.getTextRange().equalsIgnoreCase(observation.getValueString()) ||
                                    range.getTextRange().equalsIgnoreCase(observation.getValueCode())))
                    .findFirst()
                    .orElse(null);
            if (matchingRange != null) {
                observation.setInterpretationCode(matchingRange.getInterpretationCode());
                observation.setReferenceRange(matchingRange);
            } else {
                observation.setInterpretationCode("N"); // Default to normal if no specific rule
            }
        }
        observation.setInterpretationSystem("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation");
    }

    /**
     * Applies advanced interpretation rules from the test_interpretation_rules table.
     * This would typically be more complex, involving evaluating a condition expression.
     * @param observation The observation to interpret.
     */
    private void applyInterpretationRules(Observation observation) {
        // This is a placeholder for a rule engine.
        // A real implementation would:
        // 1. Fetch rules for the observation.getAnalyte() from testInterpretationRuleRepository.
        // 2. Evaluate the 'condition_expression' string against the actual observation values.
        //    This might involve a scripting engine (like Nashorn/GraalVM JS, MVEL, or custom parser)
        //    or pre-parsing conditions into executable Java code.
        // 3. Set auto_comment, trigger reflex actions, and priority.

        List<TestInterpretationRule> rules = testInterpretationRuleRepository.findByAnalyte(observation.getAnalyte());
        for (TestInterpretationRule rule : rules) {
            // Placeholder: simplified rule evaluation (e.g., if it's a critical rule)
            if ("Critical".equalsIgnoreCase(rule.getPriority()) && "H".equals(observation.getInterpretationCode())) { // Example simple rule
                System.out.println("CRITICAL ALERT: Rule matched for " + observation.getAnalyte().getAnalyteName() + ": " + rule.getAutoComment());
                // In real app, send notification, change observation status to 'amended' for review, etc.
            }
            // Add other rule evaluation logic here
        }
    }


    private String generateLocalObservationId() {
        return "OBS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


    private ObservationResponse mapToObservationResponse(Observation observation) {
        ObservationResponse response = new ObservationResponse();
        response.setId(String.format("obs-%s", observation.getId()));
        response.setServiceRequestId(String.valueOf(observation.getServiceRequest().getId()));
        response.setSpecimenId(String.format("spec-%s", observation.getSpecimen().getId()));
        response.setTestName(observation.getAnalyte().getParentTest().getTestName());
        response.setAnalyteId(String.format("an-%s", observation.getAnalyte().getId()));
        response.setAnalyteName(observation.getAnalyte().getAnalyteName());
        response.setValueNumeric(observation.getValueNumeric());
        response.setValueString(observation.getValueString());
        response.setUnit(Objects.nonNull(observation.getUnit()) ? observation.getUnit().getName() : "");

        ReferenceRange referenceRange = observation.getReferenceRange();
        if (Objects.nonNull(referenceRange)) {
            if (Objects.nonNull(referenceRange.getTextRange())) {
                response.setReferenceRange(referenceRange.getTextRange());
            } else {
                response.setReferenceRange(String.format("%s - %s", referenceRange.getLowValue(), referenceRange.getHighValue()));
            }
        }

        OrganizationAnalyteInterpretationRule interpretationRule =
                organizationAnalyteInterpretationRuleRepository.findByAnalyteIdAndOrganizationId(
                        observation.getAnalyte().getId(),
                        observation.getPatient().getOrganization().getId()
                );
        if (Objects.nonNull(interpretationRule)) {
            response.setInterpretation(interpretationRule.getAutoComment());
        }

        response.setEffectiveDateTime(observation.getEffectiveDateTime());
        return response;
    }

    // TODO: Add methods for retrieving observations, searching, etc.
    @Transactional(readOnly = true)
    public List<ObservationResponse> getObservationsByServiceRequestId(Integer serviceRequestId) {

        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new RuntimeException("Service Request not found with ID: " + serviceRequestId));

        // --- Multi-tenancy check (internal) ---
        Integer organizationId = serviceRequest.getPatient().getOrganization().getId();
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new AccessDeniedException("User not authorized to view observations for organization ID: " + organizationId);
        }
        // --- End multi-tenancy check ---
        return observationRepository.findByServiceRequestId(serviceRequestId)
                .stream()
                .map(this::mapToObservationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ObservationResponse getObservationById(Integer observationId) {
        Observation observation = observationRepository.findById(observationId)
                .orElseThrow(() -> new RuntimeException("Observation not found with ID: " + observationId));

        // --- Multi-tenancy check (internal) ---
        Integer organizationId = observation.getPatient().getOrganization().getId();
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new AccessDeniedException("User not authorized to view observation for organization ID: " + organizationId);
        }
        // --- End multi-tenancy check ---
        return mapToObservationResponse(observation);
    }
}

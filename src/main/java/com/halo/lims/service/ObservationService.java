package com.halo.lims.service;

import com.halo.lims.constant.EncounterStatus;
import com.halo.lims.dto.observation.ObservationCreateRequest;
import com.halo.lims.dto.observation.ObservationResponse;
import com.halo.lims.dto.observation.ObservationUpdateRequest;
import com.halo.lims.model.*;
import com.halo.lims.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.halo.lims.dto.observation.ObservationHistoryPointResponse;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObservationService {

    private final ObservationRepository observationRepository;
    private final TestAnalyteRepository testAnalyteRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final SpecimenRepository specimenRepository;
    private final PractitionerRepository practitionerRepository;
    private final EncounterRepository encounterRepository;
    private final ReferenceRangeRepository referenceRangeRepository;
    private final ReportApprovalService reportApprovalService;

    @Transactional
    public ObservationResponse createObservation(ObservationCreateRequest request, Integer performerId) {
        log.info("Creating observation for service request: {}", request.getServiceRequestId());

        ServiceRequest sr = serviceRequestRepository.findById(request.getServiceRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Service Request not found"));

        TestAnalyte analyte = testAnalyteRepository.findById(request.getAnalyteId())
                .orElseThrow(() -> new IllegalArgumentException("Analyte not found"));

        Practitioner performer = practitionerRepository.findById(performerId)
                .orElseThrow(() -> new IllegalArgumentException("Performer not found"));

        Specimen specimen = null;
        if (request.getSpecimenId() != null) {
            specimen = specimenRepository.findById(request.getSpecimenId()).orElse(null);
        }

        Observation obs = Observation.builder()
                .serviceRequest(sr)
                .patient(sr.getPatient())
                .analyte(analyte)
                .specimen(specimen)
                .performer(performer)
                .status("preliminary")
                .valueNumeric(request.getValueNumeric())
                .valueString(request.getValueString())
                .valueCode(request.getValueCode())
                .valueCodeSystem(request.getValueCodeSystem())
                .comments(request.getComments())
                .effectiveDateTime(request.getEffectiveDateTime() != null ? request.getEffectiveDateTime() : OffsetDateTime.now())
                .issuedDateTime(OffsetDateTime.now())
                .localObservationSystem("LIMS")
                .localObservationValue(UUID.randomUUID().toString())
                .unit(analyte.getUnit())
                .build();

        applyReferenceRangeInterpretation(obs);

        Observation saved = observationRepository.save(obs);
        return mapToResponse(saved);
    }

    @Transactional
    public ObservationResponse updateObservation(Integer id, ObservationUpdateRequest request, Integer performerId) {
        log.info("Updating observation: {}", id);

        Observation obs = observationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Observation not found"));

        if ("final".equals(obs.getStatus())) {
            throw new IllegalStateException("Cannot update finalized observation");
        }

        obs.setValueNumeric(request.getValueNumeric());
        obs.setValueString(request.getValueString());
        obs.setValueCode(request.getValueCode());
        obs.setValueCodeSystem(request.getValueCodeSystem());
        obs.setComments(request.getComments());
        obs.setInterpretationCode(request.getInterpretationCode());
        if (request.getEffectiveDateTime() != null) {
            obs.setEffectiveDateTime(request.getEffectiveDateTime());
        }

        applyReferenceRangeInterpretation(obs);

        Observation saved = observationRepository.save(obs);
        return mapToResponse(saved);
    }

    @Transactional
    public List<ObservationResponse> sendForVerification(List<Integer> observationIds, Integer technicianId) {
        log.info("Sending observations for verification: {}", observationIds);

        List<Observation> observations = observationRepository.findAllById(observationIds);
        for (Observation obs : observations) {
            // If already pending-verification or final, skip instead of error
            if ("pending-verification".equalsIgnoreCase(obs.getStatus()) || "final".equalsIgnoreCase(obs.getStatus())) {
                continue;
            }
            obs.setStatus("pending-verification");
        }

        List<Observation> saved = observationRepository.saveAll(observations);

        // Promote encounter to PENDING_VERIFICATION if it's currently IN_PROGRESS or ARRIVED
        observations.stream()
                .map(o -> o.getServiceRequest().getEncounter())
                .filter(java.util.Objects::nonNull)
                .distinct()
                .forEach(e -> {
                    String status = e.getStatus();
                    if ("in-progress".equalsIgnoreCase(status) || "arrived".equalsIgnoreCase(status)) {
                        e.setStatus(EncounterStatus.PENDING_VERIFICATION.getCode());
                        encounterRepository.save(e);
                        log.info("Encounter {} promoted to PENDING_VERIFICATION as observations were sent.", e.getId());
                    }
                });

        return saved.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<ObservationResponse> approveObservations(List<Integer> observationIds, Integer pathologistId) {
        log.info("Approving observations: {}", observationIds);

        List<Observation> observations = observationRepository.findAllById(observationIds);
        if (observations.isEmpty()) return new ArrayList<>();

        Practitioner pathologist = practitionerRepository.findById(pathologistId)
                .orElseThrow(() -> new IllegalArgumentException("Pathologist not found"));

        for (Observation obs : observations) {
            obs.setStatus("final");
            obs.setIssuedDateTime(OffsetDateTime.now());
            obs.setPerformer(pathologist); // Record the approving pathologist
        }

        List<Observation> saved = observationRepository.saveAll(observations);

        // Check if all observations for the associated encounters are finalized
        observations.stream()
                .map(o -> o.getServiceRequest().getEncounter())
                .filter(java.util.Objects::nonNull)
                .distinct()
                .forEach(e -> promoteEncounterIfAllObservationsFinal(e, pathologist));

        return saved.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private void promoteEncounterIfAllObservationsFinal(Encounter encounter, Practitioner approver) {
        List<ServiceRequest> srs = serviceRequestRepository.findByEncounter(encounter);
        if (srs.isEmpty()) {
            return;
        }

        boolean allRequestsReady = true;
        for (ServiceRequest sr : srs) {
            List<Observation> srObs = observationRepository.findByServiceRequestId(sr.getId());
            if (srObs.isEmpty()) {
                log.info("Encounter {} cannot be promoted to APPROVED: Service Request {} has no observations.", encounter.getId(), sr.getLocalOrderValue());
                allRequestsReady = false;
                break;
            }
            boolean srFinal = srObs.stream().allMatch(o -> "final".equalsIgnoreCase(o.getStatus()));
            if (!srFinal) {
                log.info("Encounter {} cannot be promoted to APPROVED: Service Request {} has non-final observations.", encounter.getId(), sr.getLocalOrderValue());
                allRequestsReady = false;
                break;
            }
        }

        if (allRequestsReady) {
            log.info("All observations final for encounter {}. Promoting to APPROVED.", encounter.getId());
            encounter.setStatus(EncounterStatus.APPROVED.getCode());
            encounter.setApprovingPractitioner(approver);
            encounterRepository.save(encounter);
        }
    }

    public Map<String, List<ObservationHistoryPointResponse>> getHistoricalObservationSeriesByServiceRequestId(Integer serviceRequestId, int limit) {
        ServiceRequest sr = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Service Request not found"));

        List<Observation> currentObs = observationRepository.findByServiceRequestId(serviceRequestId);
        List<Integer> analyteIds = currentObs.stream().map(o -> o.getAnalyte().getId()).distinct().collect(Collectors.toList());

        if (analyteIds.isEmpty()) return new HashMap<>();

        List<Observation> historical = observationRepository.findHistoricalByAnalyteIdsAndPatientAndOrganization(
                analyteIds,
                sr.getPatient().getId(),
                sr.getPatient().getOrganization().getId(),
                serviceRequestId
        );

        Map<String, List<ObservationHistoryPointResponse>> result = new HashMap<>();
        
        // Include current values if they are numeric
        for (Observation o : currentObs) {
            if (o.getValueNumeric() != null) {
                String key = o.getAnalyte().getId().toString();
                result.computeIfAbsent(key, k -> new ArrayList<>())
                      .add(new ObservationHistoryPointResponse(
                              o.getValueNumeric().doubleValue(),
                              o.getEffectiveDateTime().toString(),
                              o.getId()
                      ));
            }
        }

        // Group historical values
        for (Observation o : historical) {
            String key = o.getAnalyte().getId().toString();
            List<ObservationHistoryPointResponse> series = result.get(key);
            if (series != null && series.size() < limit) {
                series.add(new ObservationHistoryPointResponse(
                        o.getValueNumeric().doubleValue(),
                        o.getEffectiveDateTime().toString(),
                        o.getId()
                ));
            }
        }

        return result;
    }

    public List<ObservationResponse> getObservationsByServiceRequestId(Integer serviceRequestId) {
        return observationRepository.findByServiceRequestId(serviceRequestId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ObservationResponse getObservationById(Integer id) {
        return observationRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Observation not found"));
    }

    private void applyReferenceRangeInterpretation(Observation obs) {
        if (obs.getValueNumeric() == null) return;

        List<ReferenceRange> ranges = referenceRangeRepository.findByAnalyteId(obs.getAnalyte().getId());
        for (ReferenceRange range : ranges) {
            // Basic matching without gender/age for now
            if (range.getLowValue() != null && obs.getValueNumeric().compareTo(range.getLowValue()) < 0) {
                obs.setInterpretationCode("L");
                obs.setReferenceRange(range);
                return;
            }
            if (range.getHighValue() != null && obs.getValueNumeric().compareTo(range.getHighValue()) > 0) {
                obs.setInterpretationCode("H");
                obs.setReferenceRange(range);
                return;
            }
            if (range.getLowValue() != null && range.getHighValue() != null) {
                obs.setInterpretationCode("N");
                obs.setReferenceRange(range);
                return;
            }
        }
    }

    private ObservationResponse mapToResponse(Observation obs) {
        ObservationResponse resp = new ObservationResponse();
        resp.setId(obs.getId().toString());
        resp.setServiceRequestId(obs.getServiceRequest().getId().toString());
        if (obs.getSpecimen() != null) {
            resp.setSpecimenId(obs.getSpecimen().getId().toString());
        }
        resp.setAnalyteId(obs.getAnalyte().getId().toString());
        resp.setAnalyteName(obs.getAnalyte().getAnalyteName());
        resp.setTestName(obs.getAnalyte().getParentTest().getTestName());
        resp.setValueNumeric(obs.getValueNumeric());
        resp.setValueString(obs.getValueString());
        if (obs.getUnit() != null) {
            resp.setUnit(obs.getUnit().getName());
        }
        if (obs.getReferenceRange() != null) {
            resp.setReferenceRange(obs.getReferenceRange().getTextRange());
        }
        resp.setInterpretation(obs.getInterpretationCode());
        resp.setComments(obs.getComments());
        resp.setEffectiveDateTime(obs.getEffectiveDateTime());
        return resp;
    }
}

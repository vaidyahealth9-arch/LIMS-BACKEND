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
        // Use the method that eagerly loads reference ranges to avoid lazy loading issues
        return observationRepository.findByServiceRequestIdWithReferences(serviceRequestId).stream()
                .map(obs -> {
                    // Fallback: ensure reference ranges are populated for display if not already loaded
                    if (obs.getReferenceRange() == null && obs.getAnalyte() != null) {
                        List<ReferenceRange> ranges = referenceRangeRepository.findByAnalyteId(obs.getAnalyte().getId());
                        if (!ranges.isEmpty()) {
                            // Use the first available reference range for display
                            obs.setReferenceRange(ranges.get(0));
                        }
                    }
                    return mapToResponse(obs);
                })
                .collect(Collectors.toList());
    }

    public ObservationResponse getObservationById(Integer id) {
        Observation obs = observationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Observation not found"));
        
        // Ensure reference ranges are populated for display
        if (obs.getReferenceRange() == null && obs.getAnalyte() != null) {
            List<ReferenceRange> ranges = referenceRangeRepository.findByAnalyteId(obs.getAnalyte().getId());
            if (!ranges.isEmpty()) {
                obs.setReferenceRange(ranges.get(0));
            }
        }
        
        return mapToResponse(obs);
    }

    private void applyReferenceRangeInterpretation(Observation obs) {
        List<ReferenceRange> ranges = referenceRangeRepository.findByAnalyteId(obs.getAnalyte().getId());
        if (ranges.isEmpty()) {
            return;
        }
        
        ReferenceRange primaryRange = ranges.get(0);
        obs.setReferenceRange(primaryRange);
        
        // Try to obtain a numeric value
        Double val = null;
        if (obs.getValueNumeric() != null) {
            val = obs.getValueNumeric().doubleValue();
        } else {
            String strVal = obs.getValueString();
            if (strVal == null || strVal.isBlank()) {
                strVal = obs.getValueCode();
            }
            if (strVal != null && !strVal.isBlank()) {
                try {
                    val = Double.parseDouble(strVal.trim());
                } catch (NumberFormatException e) {
                    // Not numeric, handle as qualitative/titer below
                }
            }
        }
        
        // Titer matching check
        String strVal = obs.getValueString();
        if (strVal == null || strVal.isBlank()) {
            strVal = obs.getValueCode();
        }
        if (strVal != null && !strVal.isBlank()) {
            String cleanVal = strVal.trim();
            java.util.regex.Matcher patientTiterMatcher = java.util.regex.Pattern.compile("1\\s*:\\s*([0-9]+)").matcher(cleanVal);
            if (patientTiterMatcher.find()) {
                int patientDenominator = Integer.parseInt(patientTiterMatcher.group(1));
                String textRange = primaryRange.getTextRange();
                if (textRange != null && !textRange.isBlank()) {
                    java.util.regex.Matcher thresholdMatcher = java.util.regex.Pattern.compile("(?:Significant|Reactive|Abnormal)?\\s*(?:>=|≥|>=)\\s*1\\s*:\\s*([0-9]+)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(textRange);
                    if (thresholdMatcher.find()) {
                        int thresholdDenominator = Integer.parseInt(thresholdMatcher.group(1));
                        if (patientDenominator >= thresholdDenominator) {
                            obs.setInterpretationCode("H");
                        } else {
                            obs.setInterpretationCode("N");
                        }
                        return;
                    }
                }
            }
        }

        String textRange = primaryRange.getTextRange();
        if (textRange == null || textRange.isBlank()) {
            textRange = obs.getAnalyte().getBiologicalRefInterval();
        }
        
        Double dbLow = (primaryRange.getLowValue() != null) ? primaryRange.getLowValue().doubleValue() : null;
        Double dbHigh = (primaryRange.getHighValue() != null) ? primaryRange.getHighValue().doubleValue() : null;
        
        if (val != null) {
            // Demographic properties
            String gender = (obs.getPatient() != null) ? obs.getPatient().getGender() : null;
            int ageYears = -1;
            if (obs.getPatient() != null && obs.getPatient().getDateOfBirth() != null) {
                try {
                    ageYears = java.time.Period.between(obs.getPatient().getDateOfBirth(), java.time.LocalDate.now()).getYears();
                } catch (Exception e) {
                    // Ignore
                }
            }

            if (textRange != null && !textRange.isBlank()) {
                // Split parts by |, ;, or newlines, or commas
                String[] splitParts = textRange.split("[|;\\n,]");
                List<String> parts = new ArrayList<>();
                for (String part : splitParts) {
                    if (!part.isBlank()) {
                        parts.add(part.trim());
                    }
                }

                // Demographic filtering
                List<String> filteredParts = new ArrayList<>();
                boolean hasSpecificParts = false;
                for (String part : parts) {
                    boolean genderMatch = matchesGender(part, gender);
                    boolean ageMatch = matchesAge(part, ageYears);
                    
                    String partLower = part.toLowerCase();
                    boolean isSpecific = partLower.matches(".*\\b(male|males|men|man|female|females|women|woman|yr|year|age)\\b.*");
                    if (isSpecific) {
                        hasSpecificParts = true;
                    }
                    if (genderMatch && ageMatch) {
                        filteredParts.add(part);
                    }
                }

                if (filteredParts.isEmpty() && hasSpecificParts) {
                    // fallback to all parts if no match
                    filteredParts = parts;
                } else if (!filteredParts.isEmpty()) {
                    parts = filteredParts;
                }

                // Extract condition-label pairs
                class ConditionLabel {
                    String conditionText;
                    String label;
                    ConditionLabel(String c, String l) {
                        this.conditionText = c;
                        this.label = l;
                    }
                }
                List<ConditionLabel> condLabels = new ArrayList<>();
                for (String part : parts) {
                    int colonIdx = part.indexOf(':');
                    if (colonIdx != -1) {
                        String left = part.substring(0, colonIdx).trim();
                        String right = part.substring(colonIdx + 1).trim();
                        
                        boolean leftHasNumericOrOp = left.matches(".*(?:<|>|≤|≥|=|\\bto\\b|[0-9]).*");
                        boolean rightHasNumericOrOp = right.matches(".*(?:<|>|≤|≥|=|\\bto\\b|[0-9]).*");
                        
                        if (leftHasNumericOrOp && !rightHasNumericOrOp) {
                            condLabels.add(new ConditionLabel(left, right));
                        } else if (rightHasNumericOrOp && !leftHasNumericOrOp) {
                            condLabels.add(new ConditionLabel(right, left));
                        } else {
                            condLabels.add(new ConditionLabel(right, left));
                        }
                    } else {
                        java.util.regex.Matcher mRange = java.util.regex.Pattern.compile("([0-9.]+)\\s*(?:-|–|—|to)\\s*([0-9.]+)").matcher(part);
                        java.util.regex.Matcher mLimit = java.util.regex.Pattern.compile("(?:<|>|≤|≥|<=|>=)\\s*([0-9.]+)").matcher(part);
                        
                        if (mRange.find()) {
                            String cond = mRange.group();
                            String label = part.replace(cond, "").replaceAll("[^a-zA-Z\\s]", "").trim();
                            condLabels.add(new ConditionLabel(cond, label));
                        } else if (mLimit.find()) {
                            String cond = mLimit.group();
                            String label = part.replace(cond, "").replaceAll("[^a-zA-Z\\s]", "").trim();
                            condLabels.add(new ConditionLabel(cond, label));
                        } else {
                            condLabels.add(new ConditionLabel("", part));
                        }
                    }
                }

                // Evaluate conditions
                Double minLow = null;
                Double maxHigh = null;
                boolean matchedAny = false;
                String matchedCode = null;

                for (ConditionLabel cl : condLabels) {
                    String cond = cl.conditionText.trim();
                    String label = cl.label.trim();
                    String code = mapLabelToInterpretationCode(label);
                    
                    Double lowLimit = null;
                    Double highLimit = null;
                    boolean matchesVal = false;
                    
                    java.util.regex.Matcher mRange = java.util.regex.Pattern.compile("([0-9.]+)\\s*(?:-|–|—|to)\\s*([0-9.]+)").matcher(cond);
                    if (mRange.find()) {
                        lowLimit = Double.parseDouble(mRange.group(1));
                        highLimit = Double.parseDouble(mRange.group(2));
                        if (val >= lowLimit && val <= highLimit) {
                            matchesVal = true;
                        }
                    } else {
                        java.util.regex.Matcher mLess = java.util.regex.Pattern.compile("(?:<|≤|<=)\\s*([0-9.]+)").matcher(cond);
                        if (mLess.find()) {
                            highLimit = Double.parseDouble(mLess.group(1));
                            if (cond.contains("<") && !cond.contains("<=")) {
                                if (val < highLimit) matchesVal = true;
                            } else {
                                if (val <= highLimit) matchesVal = true;
                            }
                        } else {
                            java.util.regex.Matcher mGreater = java.util.regex.Pattern.compile("(?:>|≥|>=)\\s*([0-9.]+)").matcher(cond);
                            if (mGreater.find()) {
                                lowLimit = Double.parseDouble(mGreater.group(1));
                                if (cond.contains(">") && !cond.contains(">=")) {
                                    if (val > lowLimit) matchesVal = true;
                                } else {
                                    if (val >= lowLimit) matchesVal = true;
                                }
                            } else {
                                java.util.regex.Matcher mUpTo = java.util.regex.Pattern.compile("up\\s+to\\s+([0-9.]+)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(cond);
                                if (mUpTo.find()) {
                                    highLimit = Double.parseDouble(mUpTo.group(1));
                                    if (val <= highLimit) matchesVal = true;
                                }
                            }
                        }
                    }
                    
                    if ("N".equals(code)) {
                        if (lowLimit != null) {
                            if (minLow == null || lowLimit < minLow) {
                                minLow = lowLimit;
                            }
                        }
                        if (highLimit != null) {
                            if (maxHigh == null || highLimit > maxHigh) {
                                maxHigh = highLimit;
                            }
                        }
                    }
                    
                    if (matchesVal) {
                        matchedAny = true;
                        if (matchedCode == null || "N".equals(matchedCode)) {
                            matchedCode = code;
                        }
                    }
                }

                if (matchedAny) {
                    obs.setInterpretationCode(matchedCode);
                    return;
                }

                if (minLow != null && val < minLow) {
                    obs.setInterpretationCode("L");
                    return;
                }
                if (maxHigh != null && val > maxHigh) {
                    obs.setInterpretationCode("H");
                    return;
                }
            }
            
            // Fallback to database bounds
            if (dbLow != null || dbHigh != null) {
                if (dbLow != null && val < dbLow) {
                    obs.setInterpretationCode("L");
                } else if (dbHigh != null && val > dbHigh) {
                    obs.setInterpretationCode("H");
                } else {
                    obs.setInterpretationCode("N");
                }
                return;
            }
        }
        
        // Handle qualitative/text string interpretation
        if (strVal != null && !strVal.isBlank()) {
            String cleanVal = strVal.trim().toLowerCase();
            
            if (cleanVal.equals("negative") || cleanVal.equals("absent") || cleanVal.equals("non-reactive") || 
                cleanVal.equals("non reactive") || cleanVal.equals("normal") || cleanVal.equals("nil") ||
                cleanVal.contains("not detected") || cleanVal.equals("undetected") || cleanVal.equals("clear") ||
                cleanVal.equals("healthy")) {
                obs.setInterpretationCode("N");
            } else if (cleanVal.equals("positive") || cleanVal.equals("reactive") || cleanVal.equals("present") || 
                       cleanVal.equals("abnormal") || cleanVal.contains("reactive") || cleanVal.contains("detected") ||
                       cleanVal.equals("1+") || cleanVal.equals("2+") || cleanVal.equals("3+") || cleanVal.equals("4+")) {
                obs.setInterpretationCode("H");
            } else {
                if (obs.getInterpretationCode() == null) {
                    obs.setInterpretationCode("N");
                }
            }
        } else {
            if (obs.getInterpretationCode() == null) {
                obs.setInterpretationCode("N");
            }
        }
    }

    private boolean matchesGender(String text, String patientGender) {
        if (patientGender == null) return true;
        String clean = text.toLowerCase();
        boolean mentionsMale = clean.matches(".*\\b(male|males|men|man)\\b.*");
        boolean mentionsFemale = clean.matches(".*\\b(female|females|women|woman)\\b.*");
        
        if ("male".equalsIgnoreCase(patientGender)) {
            if (mentionsFemale && !mentionsMale) {
                return false;
            }
            if (mentionsMale && !mentionsFemale) {
                return true;
            }
        } else if ("female".equalsIgnoreCase(patientGender)) {
            if (mentionsMale && !mentionsFemale) {
                return false;
            }
            if (mentionsFemale && !mentionsMale) {
                return true;
            }
        }
        return true;
    }

    private boolean matchesAge(String text, int ageYears) {
        if (ageYears < 0) return true;
        String clean = text.toLowerCase();
        
        java.util.regex.Matcher mUnder = java.util.regex.Pattern.compile("(?:<|≤|<=|under|less\\s+than)\\s*([0-9]+)\\s*(?:yr|year|yr|age)").matcher(clean);
        if (mUnder.find()) {
            int limit = Integer.parseInt(mUnder.group(1));
            return ageYears < limit;
        }
        
        java.util.regex.Matcher mOver = java.util.regex.Pattern.compile("(?:>|≥|>=|over|above|greater\\s+than)\\s*([0-9]+)\\s*(?:yr|year|yr|age)").matcher(clean);
        if (mOver.find()) {
            int limit = Integer.parseInt(mOver.group(1));
            return ageYears > limit;
        }
        
        return true;
    }

    private String mapLabelToInterpretationCode(String label) {
        if (label == null) return "N";
        String clean = label.trim().toLowerCase();
        
        if (clean.contains("normal") || clean.contains("negative") || clean.contains("absent") ||
            clean.contains("non-reactive") || clean.contains("non reactive") || clean.contains("nil") ||
            clean.contains("sufficiency") || clean.contains("sufficient") || clean.contains("desirable") ||
            clean.contains("optimal") || clean.contains("non-diabetic") || clean.contains("non-smokers") ||
            clean.contains("control") || clean.contains("clear")) {
            return "N";
        }
        
        if (clean.contains("deficiency") || clean.contains("insufficiency") || clean.contains("low") ||
            clean.contains("decreased") || clean.contains("below")) {
            return "L";
        }
        
        if (clean.contains("positive") || clean.contains("reactive") || clean.contains("present") ||
            clean.contains("abnormal") || clean.contains("toxicity") || clean.contains("high") ||
            clean.contains("significant") || clean.contains("diabetic") || clean.contains("diabetes") ||
            clean.contains("smokers") || clean.contains("elevated") || clean.contains("above") ||
            clean.contains("indeterminate") || clean.contains("equivocal") || clean.contains("borderline") ||
            clean.contains("impaired") || clean.contains("pre-diabetic") || clean.contains("very high")) {
            return "H";
        }
        
        return "N";
    }

    private ObservationResponse mapToResponse(Observation obs) {
        ObservationResponse resp = new ObservationResponse();
        if (obs.getId() != null) {
            resp.setId(obs.getId().toString());
        }
        if (obs.getServiceRequest() != null && obs.getServiceRequest().getId() != null) {
            resp.setServiceRequestId(obs.getServiceRequest().getId().toString());
        }
        if (obs.getSpecimen() != null && obs.getSpecimen().getId() != null) {
            resp.setSpecimenId(obs.getSpecimen().getId().toString());
        }
        if (obs.getAnalyte() != null && obs.getAnalyte().getId() != null) {
            resp.setAnalyteId(obs.getAnalyte().getId().toString());
        }
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

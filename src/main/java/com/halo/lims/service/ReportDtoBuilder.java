package com.halo.lims.service;

import com.halo.lims.dto.report.AnalyticsSummary;
import com.halo.lims.dto.report.AnalyteResult;
import com.halo.lims.dto.report.BrandingDTO;
import com.halo.lims.dto.report.DiagnosticReportDTO;
import com.halo.lims.dto.report.DoctorSignature;
import com.halo.lims.dto.report.LongitudinalTrend;
import com.halo.lims.dto.report.PanelVolume;
import com.halo.lims.dto.report.PatientDetails;
import com.halo.lims.dto.report.ReportApprovalStatusResponse;
import com.halo.lims.dto.report.ReportMetadataDTO;
import com.halo.lims.dto.report.TestGroup;
import com.halo.lims.model.Observation;
import com.halo.lims.model.Organization;
import com.halo.lims.model.OrganizationAnalyteInterpretationRule;
import com.halo.lims.model.OrganizationTestInterpretationRule;
import com.halo.lims.model.Patient;
import com.halo.lims.model.Practitioner;
import com.halo.lims.model.ReferenceRange;
import com.halo.lims.model.ServiceRequest;
import com.halo.lims.model.Specimen;
import com.halo.lims.model.SpecimenType;
import com.halo.lims.model.Test;
import com.halo.lims.model.TestAnalyte;
import com.halo.lims.repository.ObservationRepository;
import com.halo.lims.repository.OrganizationAnalyteInterpretationRuleRepository;
import com.halo.lims.repository.OrganizationTestInterpretationRuleRepository;
import com.halo.lims.repository.ServiceRequestRepository;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReportDtoBuilder {

    private static final String REPORT_TYPE_SMART = "smart";
    private static final ExpressionParser spelParser = new SpelExpressionParser();

    private final ServiceRequestRepository serviceRequestRepository;
    private final ObservationRepository observationRepository;
    private final OrganizationTestInterpretationRuleRepository organizationTestInterpretationRuleRepository;
    private final OrganizationAnalyteInterpretationRuleRepository organizationAnalyteInterpretationRuleRepository;
    private final ReportApprovalService reportApprovalService;
    private final ImageService imageService;

    public ReportDtoBuilder(
            ServiceRequestRepository serviceRequestRepository,
            ObservationRepository observationRepository,
            OrganizationTestInterpretationRuleRepository organizationTestInterpretationRuleRepository,
            OrganizationAnalyteInterpretationRuleRepository organizationAnalyteInterpretationRuleRepository,
            ReportApprovalService reportApprovalService,
            ImageService imageService) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.observationRepository = observationRepository;
        this.organizationTestInterpretationRuleRepository = organizationTestInterpretationRuleRepository;
        this.organizationAnalyteInterpretationRuleRepository = organizationAnalyteInterpretationRuleRepository;
        this.reportApprovalService = reportApprovalService;
        this.imageService = imageService;
    }

    @Transactional(readOnly = true)
    public DiagnosticReportDTO buildReportDTO(Integer serviceRequestId, boolean withHeader,
                                              String normalizedReportType,
                                              ReportApprovalStatusResponse approvalStatus) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new RuntimeException("Service Request not found: " + serviceRequestId));

        // Eagerly load observations with references, unit, analyte, and specimen to avoid lazy loading issues
        List<Observation> observations = observationRepository.findByServiceRequestIdWithReferences(serviceRequestId)
                .stream().sorted(Comparator.comparing(Observation::getId)).toList();

        Patient patient = serviceRequest.getPatient();
        Organization organization = patient != null ? patient.getOrganization() : null;

        Map<Integer, List<BigDecimal>> historyMap = buildHistoryMap(
                observations, serviceRequestId, patient, organization, normalizedReportType);

        BrandingDTO branding = buildBrandingDTO(organization, withHeader);
        PatientDetails patientDetails = buildPatientDetails(patient, serviceRequest, observations, approvalStatus);

        // Batch fetch interpretation rules to eliminate N+1 queries
        List<Test> tests = observations.stream()
                .map(obs -> obs.getAnalyte() != null ? obs.getAnalyte().getParentTest() : null)
                .filter(Objects::nonNull).distinct().toList();
        List<TestAnalyte> analytes = observations.stream()
                .map(Observation::getAnalyte).filter(Objects::nonNull).distinct().toList();

        Map<Integer, List<OrganizationTestInterpretationRule>> testRulesMap = new HashMap<>();
        Map<Integer, List<OrganizationAnalyteInterpretationRule>> analyteRulesMap = new HashMap<>();

        if (organization != null) {
            if (!tests.isEmpty()) {
                List<OrganizationTestInterpretationRule> tRules = organizationTestInterpretationRuleRepository
                        .findByOrganizationTestOrganizationAndOrganizationTestTestIn(organization, tests);
                testRulesMap = tRules.stream()
                        .filter(rule -> rule.getOrganizationTest() != null && rule.getOrganizationTest().getTest() != null)
                        .collect(Collectors.groupingBy(rule -> rule.getOrganizationTest().getTest().getId()));
            }
            if (!analytes.isEmpty()) {
                List<OrganizationAnalyteInterpretationRule> aRules = organizationAnalyteInterpretationRuleRepository
                        .findByOrganizationAndAnalyteIn(organization, analytes);
                analyteRulesMap = aRules.stream()
                        .filter(rule -> rule.getAnalyte() != null)
                        .collect(Collectors.groupingBy(rule -> rule.getAnalyte().getId()));
            }
        }

        List<TestGroup> testGroups = buildTestGroups(serviceRequest, observations, historyMap, testRulesMap, analyteRulesMap);
        DoctorSignature signature = buildDoctorSignature(approvalStatus, observations);

        ReportApprovalService.ReportMetadata meta = reportApprovalService.buildReportMetadata(
                serviceRequest, approvalStatus.getApprovedAt());
        ReportMetadataDTO metadata = new ReportMetadataDTO(
                meta.ulrNumber(),
                imageService.buildQrImageUrl(meta.accreditationScopeQrContent()),
                imageService.buildQrImageUrl(meta.reportIntegrityQrContent()),
                meta.accreditationScopeQrContent(),
                meta.reportIntegrityQrContent(),
                reportApprovalService.formatDateTime(OffsetDateTime.now())
        );

        AnalyticsSummary analytics = null;
        List<LongitudinalTrend> trends = null;
        List<String> insights = new ArrayList<>();
        if (REPORT_TYPE_SMART.equals(normalizedReportType)) {
            analytics = buildAnalyticsSummary(testGroups, observations);
            trends = List.of();
            insights = buildSmartInsights(testGroups, analytics);
        }

        String reportTitle = REPORT_TYPE_SMART.equals(normalizedReportType)
                ? "Smart Diagnostic Report" : "Diagnostic Report";

        return new DiagnosticReportDTO(
                normalizedReportType, reportTitle,
                patientDetails, testGroups,
                signature, analytics, trends,
                insights, metadata, branding, withHeader
        );
    }

    private Map<Integer, List<BigDecimal>> buildHistoryMap(List<Observation> observations,
                                                           Integer serviceRequestId,
                                                           Patient patient,
                                                           Organization organization,
                                                           String reportType) {
        if (!REPORT_TYPE_SMART.equals(reportType) || patient == null || organization == null) {
            return Map.of();
        }

        List<Integer> analyteIds = observations.stream()
                .filter(obs -> obs.getAnalyte() != null && obs.getValueNumeric() != null)
                .map(obs -> obs.getAnalyte().getId())
                .distinct()
                .toList();

        if (analyteIds.isEmpty()) return Map.of();

        List<Observation> allHistory = observationRepository.findHistoricalByAnalyteIdsAndPatientAndOrganization(
                analyteIds, patient.getId(), organization.getId(), serviceRequestId);

        Map<Integer, List<Observation>> histByAnalyte = allHistory.stream()
                .filter(obs -> obs.getAnalyte() != null)
                .collect(Collectors.groupingBy(obs -> obs.getAnalyte().getId()));

        Map<Integer, List<BigDecimal>> map = new HashMap<>();
        for (Observation obs : observations) {
            if (obs.getAnalyte() == null || obs.getValueNumeric() == null) continue;
            int analyteId = obs.getAnalyte().getId();

            List<BigDecimal> values = histByAnalyte.getOrDefault(analyteId, List.of()).stream()
                    .sorted(Comparator.comparing(Observation::getEffectiveDateTime,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(10)
                    .sorted(Comparator.comparing(Observation::getEffectiveDateTime,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(Observation::getValueNumeric)
                    .collect(Collectors.toCollection(ArrayList::new));
            values.add(obs.getValueNumeric());
            map.put(analyteId, values);
        }
        return map;
    }

    private List<TestGroup> buildTestGroups(ServiceRequest serviceRequest,
                                            List<Observation> observations,
                                            Map<Integer, List<BigDecimal>> historyMap,
                                            Map<Integer, List<OrganizationTestInterpretationRule>> testRulesMap,
                                            Map<Integer, List<OrganizationAnalyteInterpretationRule>> analyteRulesMap) {
        Map<Test, List<Observation>> grouped = observations.stream()
                .filter(obs -> obs.getAnalyte() != null && obs.getAnalyte().getParentTest() != null)
                .collect(Collectors.groupingBy(obs -> obs.getAnalyte().getParentTest(), LinkedHashMap::new, Collectors.toList()));

        List<TestGroup> groups = new ArrayList<>();
        for (Map.Entry<Test, List<Observation>> entry : grouped.entrySet()) {
            Test test = entry.getKey();
            List<Observation> obsList = entry.getValue();

            List<AnalyteResult> analytes = obsList.stream()
                    .map(obs -> buildAnalyteResult(obs, historyMap.getOrDefault(obs.getAnalyte().getId(), List.of()), analyteRulesMap))
                    .toList();

            boolean hasAbnormal = analytes.stream().anyMatch(AnalyteResult::isAbnormal);
            String interpretation = buildTestInterpretation(serviceRequest, test, obsList, testRulesMap);

            groups.add(new TestGroup(test.getTestName(), analytes.size(), hasAbnormal, interpretation, analytes));
        }
        return groups;
    }

    private AnalyteResult buildAnalyteResult(Observation obs, List<BigDecimal> history, Map<Integer, List<OrganizationAnalyteInterpretationRule>> rulesMap) {
        String analyteName = obs.getAnalyte() != null ? obs.getAnalyte().getAnalyteName() : "Unknown";
        
        String value = "N/A";
        if (obs.getValueString() != null) {
            value = obs.getValueString();
        } else if (obs.getValueNumeric() != null) {
            BigDecimal numVal = obs.getValueNumeric();
            int scale = 0;
            boolean scaleFound = false;
            if (obs.getReferenceRange() != null) {
                BigDecimal low = obs.getReferenceRange().getLowValue();
                BigDecimal high = obs.getReferenceRange().getHighValue();
                if (low != null) {
                    scale = Math.max(0, low.stripTrailingZeros().scale());
                    scaleFound = true;
                }
                if (high != null) {
                    int highScale = Math.max(0, high.stripTrailingZeros().scale());
                    scale = scaleFound ? Math.max(scale, highScale) : highScale;
                    scaleFound = true;
                }
            }
            if (scaleFound) {
                value = numVal.setScale(scale, java.math.RoundingMode.HALF_UP).toPlainString();
            } else {
                value = numVal.stripTrailingZeros().toPlainString();
            }
        }

        String unit = obs.getUnit() != null ? obs.getUnit().getName() : "";
        String status = obs.getInterpretationCode() != null ? obs.getInterpretationCode() : "Normal";
        boolean isAbnormal = !"N".equalsIgnoreCase(status) && !"Normal".equalsIgnoreCase(status);
        String statusClass = isAbnormal ? "status-abnormal" : "status-normal";

        int markerPercent = computeMarkerPercent(obs);
        String refLowDisplay = obs.getReferenceRange() != null ? formatReferenceValue(obs.getReferenceRange().getLowValue()) : "";
        String refHighDisplay = obs.getReferenceRange() != null ? formatReferenceValue(obs.getReferenceRange().getHighValue()) : "";

        BigDecimal refLow = obs.getReferenceRange() != null ? obs.getReferenceRange().getLowValue() : null;
        BigDecimal refHigh = obs.getReferenceRange() != null ? obs.getReferenceRange().getHighValue() : null;
        String sparklineSvg = imageService.buildSparklineSvg(history, 350, 60, refLow, refHigh);

        // Evaluate Analyte-level rules (only if abnormal to keep report clean)
        String interpretation = "";
        if (isAbnormal && obs.getAnalyte() != null) {
            List<OrganizationAnalyteInterpretationRule> rules = rulesMap != null ? rulesMap.getOrDefault(obs.getAnalyte().getId(), List.of()) : List.of();
            interpretation = evaluateAnalyteRules(rules, obs, value);
        }

        String method = "";
        if (obs.getAnalyte() != null) {
            if (obs.getAnalyte().getMethod() != null && !obs.getAnalyte().getMethod().isEmpty()) {
                method = obs.getAnalyte().getMethod();
            } else if (obs.getAnalyte().getParentTest() != null) {
                method = obs.getAnalyte().getParentTest().getMethod();
            }
        }

        return new AnalyteResult(analyteName, value, unit, referenceRangeText(obs),
                refLowDisplay, refHighDisplay, status, statusClass, isAbnormal,
                markerPercent, history != null ? history.size() : 0, sparklineSvg, interpretation, method);
    }

    private String evaluateAnalyteRules(List<OrganizationAnalyteInterpretationRule> rules, Observation obs, String formattedValue) {
        if (rules == null || rules.isEmpty()) return "";
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("val", obs.getValueNumeric());
        context.setVariable("text", obs.getValueString());
        context.setVariable("status", obs.getInterpretationCode());
        context.setVariable("isAbnormal", !"N".equalsIgnoreCase(obs.getInterpretationCode()));

        return rules.stream()
                .filter(rule -> {
                    try {
                        Expression exp = spelParser.parseExpression(rule.getConditionExpression());
                        return Boolean.TRUE.equals(exp.getValue(context, Boolean.class));
                    } catch (Exception e) { return false; }
                })
                .map(rule -> rule.getAutoComment() != null ? rule.getAutoComment() : rule.getClassification())
                .filter(Objects::nonNull)
                .map(comment -> comment.replace("<result-value>", formattedValue).replace("<result value>", formattedValue))
                .collect(Collectors.joining("; "));
    }

    private AnalyticsSummary buildAnalyticsSummary(List<TestGroup> testGroups, List<Observation> observations) {
        int totalTests = testGroups.size();
        int totalAnalytes = observations.size();
        long abnormalCount = observations.stream().filter(this::isAbnormalObservation).count();
        int normalCount = (int) Math.max(0, totalAnalytes - abnormalCount);
        int abnormalCountInt = (int) abnormalCount;
        int abnormalPercent = totalAnalytes > 0 ? (int) Math.round((abnormalCount * 100.0) / totalAnalytes) : 0;

        List<PanelVolume> topPanels = testGroups.stream()
                .sorted(Comparator.comparingInt(TestGroup::analyteCount).reversed())
                .limit(4)
                .map(g -> {
                    int pct = totalAnalytes > 0 ? (int) Math.max(6, Math.round((g.analyteCount() * 100.0) / totalAnalytes)) : 0;
                    int abn = (int) g.analytes().stream().filter(AnalyteResult::isAbnormal).count();
                    return new PanelVolume(g.testName(), g.analyteCount(), abn, pct);
                })
                .toList();

        return new AnalyticsSummary(totalTests, totalAnalytes, normalCount, abnormalCountInt,
                100 - abnormalPercent, abnormalPercent, topPanels);
    }

    private List<String> buildInsights(List<TestGroup> testGroups, AnalyticsSummary analytics) {
        List<String> insights = new ArrayList<>();
        for (TestGroup g : testGroups) {
            if (g.hasAbnormalResults()) {
                long abnCount = g.analytes().stream().filter(AnalyteResult::isAbnormal).count();
                String interpretation = g.interpretation() == null ? "" : g.interpretation().trim();
                if (interpretation.length() > 120) {
                    interpretation = interpretation.substring(0, 117) + "...";
                }
                insights.add(abnCount + " abnormal finding(s) in " + g.testName() + ". " + interpretation);
            }
        }
        if (insights.isEmpty()) {
            insights.add("All results are within normal reference ranges.");
        }
        return insights.stream().limit(3).toList();
    }

    private BrandingDTO buildBrandingDTO(Organization org, boolean withHeader) {
        int hMm = org != null && org.getReportHeaderMarginMm() != null ? org.getReportHeaderMarginMm() : 0;
        int fMm = org != null && org.getReportFooterMarginMm() != null ? org.getReportFooterMarginMm() : 0;
        int hH = org != null && org.getReportHeaderHeightMm() != null ? org.getReportHeaderHeightMm() : 34;
        int fH = org != null && org.getReportFooterHeightMm() != null ? org.getReportFooterHeightMm() : 24;

        int pageMarginTop = withHeader ? hH : hMm;
        int pageMarginBot = withHeader ? fH : fMm;

        if (!withHeader) {
            pageMarginTop = Math.max(0, hMm);
            pageMarginBot = Math.max(0, fMm);
        }

        String header = (withHeader && org != null)
                ? safe(imageService.resolveImageUrl(org.getHeaderImageAssetId(), org.getReportHeaderImage()))
                : "";
        String footer = (withHeader && org != null)
                ? safe(imageService.resolveImageUrl(org.getFooterImageAssetId(), org.getReportFooterImage()))
                : "";
        return new BrandingDTO(header, footer, hMm, fMm, hH, fH, pageMarginTop, pageMarginBot);
    }

    private PatientDetails buildPatientDetails(Patient patient, ServiceRequest serviceRequest,
                                               List<Observation> observations,
                                               ReportApprovalStatusResponse approvalStatus) {
        String sampleId = observations.stream()
                .map(Observation::getSpecimen).filter(Objects::nonNull)
                .map(Specimen::getLocalSpecimenValue)
                .filter(v -> v != null && !v.isBlank()).findFirst().orElse("N/A");

        String referringDoctor = "N/A";
        if (serviceRequest.getEncounter() != null && serviceRequest.getEncounter().getReferenceDoctor() != null && !serviceRequest.getEncounter().getReferenceDoctor().isBlank()) {
            referringDoctor = serviceRequest.getEncounter().getReferenceDoctor();
        } else if (serviceRequest.getRequester() != null) {
            referringDoctor = reportApprovalService.buildPractitionerDisplayName(serviceRequest.getRequester());
        }

        OffsetDateTime collectionDate = observations.stream()
                .map(Observation::getEffectiveDateTime).filter(Objects::nonNull)
                .min(OffsetDateTime::compareTo).orElse(serviceRequest.getOrderDate());

        String reportDate = approvalStatus.getApprovedAt() != null
                ? reportApprovalService.formatDateTime(approvalStatus.getApprovedAt())
                : reportApprovalService.formatDateTime(OffsetDateTime.now());

        String sampleType = observations.stream()
                .map(Observation::getSpecimen).filter(Objects::nonNull)
                .map(Specimen::getSpecimenType).filter(Objects::nonNull)
                .map(SpecimenType::getName)
                .filter(v -> v != null && !v.isBlank()).findFirst().orElse("Blood");

        return new PatientDetails(
                patient != null ? getPatientName(patient) : "N/A",
                patient != null ? computeAge(patient.getDateOfBirth()) : "N/A",
                patient != null ? safe(patient.getGender()) : "N/A",
                patient != null ? safe(patient.getLocalMrnValue()) : "N/A",
                sampleId, referringDoctor,
                reportApprovalService.formatDateTime(collectionDate), reportDate,
                safe(serviceRequest.getLocalOrderValue()), sampleType
        );
    }

    private DoctorSignature buildDoctorSignature(ReportApprovalStatusResponse approvalStatus,
                                                 List<Observation> observations) {
        Practitioner practitioner = observations.stream()
                .filter(obs -> obs.getPerformer() != null)
                .max(Comparator.comparing(Observation::getIssuedDateTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(Observation::getPerformer).orElse(null);

        String qualification = (practitioner != null && practitioner.getMciRegNo() != null)
                ? "Reg. No: " + practitioner.getMciRegNo() : "";
        String approvedAt = approvalStatus.getApprovedAt() != null
                ? reportApprovalService.formatDateTime(approvalStatus.getApprovedAt()) : "";

        return new DoctorSignature(
                safe(approvalStatus.getApprovedDoctorName()),
                safe(approvalStatus.getApprovedDoctorSignatureImage()),
                qualification, approvedAt
        );
    }

    private String buildTestInterpretation(ServiceRequest serviceRequest, Test test,
                                           List<Observation> observations,
                                           Map<Integer, List<OrganizationTestInterpretationRule>> testRulesMap) {
         if (test == null) return "";
         List<OrganizationTestInterpretationRule> rules = testRulesMap.getOrDefault(test.getId(), List.of());
         
         if (!rules.isEmpty()) {
             StandardEvaluationContext context = new StandardEvaluationContext();
             // Map each analyte to its value for complex multi-analyte rules
             for (Observation obs : observations) {
                 if (obs.getAnalyte() != null && obs.getAnalyte().getAnalyteName() != null) {
                     String varName = obs.getAnalyte().getAnalyteName().replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
                     if (!varName.isBlank()) {
                         context.setVariable(varName, obs.getValueNumeric());
                         context.setVariable(varName + "_status", obs.getInterpretationCode());
                     }
                 }
             }
 
             String fromRules = rules.stream()
                     .filter(rule -> {
                         try {
                             Expression exp = spelParser.parseExpression(rule.getConditionExpression());
                             return Boolean.TRUE.equals(exp.getValue(context, Boolean.class));
                         } catch (Exception e) { return false; }
                     })
                     .map(rule -> rule.getAutoComment() != null ? rule.getAutoComment() : rule.getClassification())
                     .filter(Objects::nonNull).distinct()
                     .collect(Collectors.joining("; "));
             if (!fromRules.isBlank()) return fromRules;
         }
 
         // For clean reports, only display status summary if there is at least one abnormal analyte
         boolean hasAbnormal = observations.stream()
                 .anyMatch(obs -> obs.getInterpretationCode() != null && !"N".equalsIgnoreCase(obs.getInterpretationCode()) && !"Normal".equalsIgnoreCase(obs.getInterpretationCode()));
         
         if (hasAbnormal) {
             LinkedHashSet<String> codes = observations.stream()
                     .map(Observation::getInterpretationCode)
                     .filter(Objects::nonNull).map(String::trim).filter(c -> !c.isBlank())
                     .filter(c -> !"N".equalsIgnoreCase(c) && !"Normal".equalsIgnoreCase(c))
                     .map(this::mapInterpretationCodeToText)
                     .collect(Collectors.toCollection(LinkedHashSet::new));
             if (!codes.isEmpty()) return String.join("; ", codes);
         }
         
         return ""; // Return empty string to completely hide interpretation box for fully normal panels
     }

    private List<String> buildSmartInsights(List<TestGroup> testGroups, AnalyticsSummary analytics) {
        List<String> insights = new ArrayList<>();
        if (analytics != null && analytics.getAbnormalCount() > 0) {
            insights.add("CRITICAL: " + analytics.getAbnormalCount() + " abnormal markers detected across " + testGroups.size() + " test panels.");
            insights.add("CORRELATION: Clinical correlation with physical symptoms and medical history is advised.");
            
            // Add specific categorical recommendations (Hybrid Level 3)
            boolean hasHighCholesterol = testGroups.stream().anyMatch(g -> g.getTestName() != null && g.getTestName().toLowerCase().contains("lipid") && g.isHasAbnormalResults());
            if (hasHighCholesterol) {
                insights.add("RECOMMENDATION: Low-fat diet and regular cardiovascular exercise are recommended for lipid management.");
            }
            
            boolean hasLiverIssue = testGroups.stream().anyMatch(g -> g.getTestName() != null && g.getTestName().toLowerCase().contains("liver") && g.isHasAbnormalResults());
            if (hasLiverIssue) {
                insights.add("RECOMMENDATION: Avoid hepatotoxic substances (e.g., alcohol, certain medications) and monitor liver enzyme trends.");
            }
        } else {
            insights.add("All tested parameters are within physiological reference ranges.");
            insights.add("Maintaining a balanced diet and regular health check-ups is recommended for continued wellness.");
        }
        return insights;
    }

    private String mapInterpretationCodeToText(String code) {
        return switch (code.trim().toUpperCase(Locale.ENGLISH)) {
            case "N" -> "Within reference range";
            case "H" -> "Above reference range";
            case "HH" -> "Critically high";
            case "L" -> "Below reference range";
            case "LL" -> "Critically low";
            case "A" -> "Abnormal finding";
            default -> code;
        };
    }

    private boolean isAbnormalObservation(Observation obs) {
        return "status-abnormal".equals(buildSmartStatusClass(obs));
    }

    private boolean isCriticalObservation(Observation obs) {
        String code = safe(obs.getInterpretationCode()).toUpperCase(Locale.ENGLISH);
        if ("HH".equals(code) || "LL".equals(code)) return true;
        if (obs.getValueNumeric() != null && obs.getReferenceRange() != null) {
            BigDecimal value = obs.getValueNumeric();
            BigDecimal low = obs.getReferenceRange().getLowValue();
            BigDecimal high = obs.getReferenceRange().getHighValue();
            if (low != null && value.compareTo(low.multiply(BigDecimal.valueOf(0.8))) < 0) return true;
            if (high != null && value.compareTo(high.multiply(BigDecimal.valueOf(1.2))) > 0) return true;
        }
        return false;
    }

    private String buildSmartStatusLabel(Observation obs) {
        if (isCriticalObservation(obs)) return "Critical";
        if (isValueOutOfRange(obs)) return "Abnormal";
        String code = safe(obs.getInterpretationCode()).trim().toUpperCase(Locale.ENGLISH);
        if ("H".equals(code) || "HH".equals(code) || "L".equals(code) || "LL".equals(code) || "A".equals(code))
            return "Abnormal";
        if (obs.getValueNumeric() != null && obs.getReferenceRange() != null) {
            BigDecimal value = obs.getValueNumeric();
            BigDecimal low = obs.getReferenceRange().getLowValue();
            BigDecimal high = obs.getReferenceRange().getHighValue();
            if (low != null && high != null) {
                BigDecimal span = high.subtract(low);
                if (span.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal band = span.multiply(BigDecimal.valueOf(0.1));
                    if (value.compareTo(low.add(band)) <= 0 || value.compareTo(high.subtract(band)) >= 0)
                        return "Borderline";
                }
            }
        }
        return "Normal";
    }

    private String buildSmartStatusClass(Observation obs) {
        if (isCriticalObservation(obs) || isValueOutOfRange(obs)) return "status-abnormal";
        String code = safe(obs.getInterpretationCode()).trim().toUpperCase(Locale.ENGLISH);
        if ("H".equals(code) || "HH".equals(code) || "L".equals(code) || "LL".equals(code) || "A".equals(code))
            return "status-abnormal";
        if ("N".equals(code)) return "status-normal";
        if (obs.getValueNumeric() != null && obs.getReferenceRange() != null) {
            BigDecimal value = obs.getValueNumeric();
            BigDecimal low = obs.getReferenceRange().getLowValue();
            BigDecimal high = obs.getReferenceRange().getHighValue();
            if (low != null && high != null) {
                BigDecimal span = high.subtract(low);
                if (span.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal band = span.multiply(BigDecimal.valueOf(0.1));
                    if (value.compareTo(low.add(band)) <= 0 || value.compareTo(high.subtract(band)) >= 0)
                        return "status-borderline";
                }
            }
        }
        return isValueInRange(obs) ? "status-normal" : "";
    }

    private boolean isValueOutOfRange(Observation obs) {
        if (obs.getValueNumeric() == null || obs.getReferenceRange() == null) return false;
        BigDecimal value = obs.getValueNumeric();
        BigDecimal low = obs.getReferenceRange().getLowValue();
        BigDecimal high = obs.getReferenceRange().getHighValue();
        return (low != null && value.compareTo(low) < 0) || (high != null && value.compareTo(high) > 0);
    }

    private boolean isValueInRange(Observation obs) {
        if (obs.getValueNumeric() == null || obs.getReferenceRange() == null) return false;
        BigDecimal value = obs.getValueNumeric();
        BigDecimal low = obs.getReferenceRange().getLowValue();
        BigDecimal high = obs.getReferenceRange().getHighValue();
        if (low == null && high == null) return false;
        return (low == null || value.compareTo(low) >= 0) && (high == null || value.compareTo(high) <= 0);
    }

    private int computeMarkerPercent(Observation obs) {
        if (obs.getValueNumeric() == null || obs.getReferenceRange() == null
                || obs.getReferenceRange().getLowValue() == null
                || obs.getReferenceRange().getHighValue() == null) {
            return 50;
        }
        double low = obs.getReferenceRange().getLowValue().doubleValue();
        double high = obs.getReferenceRange().getHighValue().doubleValue();
        double val = obs.getValueNumeric().doubleValue();
        double span = high - low;
        if (span <= 0) {
            return val > high ? 90 : (val < low ? 10 : 50);
        }
        double pct = 20 + ((val - low) / span) * 60;
        return (int) Math.min(98, Math.max(2, pct));
    }

    private String formatReferenceValue(BigDecimal val) {
        if (val == null) return "";
        return val.stripTrailingZeros().toPlainString();
    }

    private String referenceRangeText(Observation obs) {
        ReferenceRange referenceRange = obs.getReferenceRange();
        if (referenceRange == null) return "";
        if (referenceRange.getTextRange() != null && !referenceRange.getTextRange().isBlank())
            return referenceRange.getTextRange();
        if (referenceRange.getLowValue() != null || referenceRange.getHighValue() != null) {
            return formatReferenceValue(referenceRange.getLowValue())
                    + " - "
                    + formatReferenceValue(referenceRange.getHighValue());
        }
        return "";
    }

    private String getPatientName(Patient patient) {
        return (safe(patient.getFirstName()) + " " + safe(patient.getLastName())).trim();
    }

    private String computeAge(LocalDate dob) {
        if (dob == null) return "N/A";
        Period period = Period.between(dob, LocalDate.now());
        if (period.getYears() == 0) {
            return period.getMonths() + " months";
        }
        return period.getYears() + " yrs";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}

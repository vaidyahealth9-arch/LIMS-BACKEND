package com.halo.lims.controller;

import com.halo.lims.model.Bill;
import com.halo.lims.model.DiagnosticReport;
import com.halo.lims.model.Patient;
import com.halo.lims.model.ServiceRequest;
import com.halo.lims.model.ServiceRequestItem;
import com.halo.lims.repository.BillRepository;
import com.halo.lims.repository.DiagnosticReportRepository;
import com.halo.lims.repository.ObservationRepository;
import com.halo.lims.repository.PatientRepository;
import com.halo.lims.repository.ServiceRequestItemRepository;
import com.halo.lims.repository.ServiceRequestRepository;
import com.halo.lims.service.InternalRequestAuthService;
import com.halo.lims.service.ReportService;
import com.halo.lims.service.ReportStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/integration/phr")
public class IntegrationController {

    private static final Logger log = LoggerFactory.getLogger(IntegrationController.class);

    private final PatientRepository patientRepository;
    private final BillRepository billRepository;
    private final DiagnosticReportRepository diagnosticReportRepository;
    private final ReportStorageService reportStorageService;
    private final ReportService reportService;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestItemRepository serviceRequestItemRepository;
    private final ObservationRepository observationRepository;
    private final InternalRequestAuthService internalRequestAuthService;
    private final String reportStorageProvider;
    private final String reportStorageLocalBasePath;

    public IntegrationController(PatientRepository patientRepository,
                                 BillRepository billRepository,
                                 DiagnosticReportRepository diagnosticReportRepository,
                                 ReportStorageService reportStorageService,
                                 ReportService reportService,
                                 ServiceRequestRepository serviceRequestRepository,
                                 ServiceRequestItemRepository serviceRequestItemRepository,
                                 ObservationRepository observationRepository,
                                 InternalRequestAuthService internalRequestAuthService,
                                 @Value("${app.report.storage.provider:local}") String reportStorageProvider,
                                 @Value("${app.report.storage.local.base-path:./data/lims-reports}") String reportStorageLocalBasePath) {
        this.patientRepository = patientRepository;
        this.billRepository = billRepository;
        this.diagnosticReportRepository = diagnosticReportRepository;
        this.reportStorageService = reportStorageService;
        this.reportService = reportService;
        this.serviceRequestRepository = serviceRequestRepository;
        this.serviceRequestItemRepository = serviceRequestItemRepository;
        this.observationRepository = observationRepository;
        this.internalRequestAuthService = internalRequestAuthService;
        this.reportStorageProvider = reportStorageProvider;
        this.reportStorageLocalBasePath = reportStorageLocalBasePath;
    }

    @GetMapping("/storage-mode")
    public ResponseEntity<Map<String, String>> getStorageMode() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("provider", reportStorageProvider);
        response.put("localBasePath", reportStorageLocalBasePath);
        response.put("serviceImplementation", reportStorageService.getClass().getSimpleName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports")
    public ResponseEntity<List<ReportIntegrationDto>> getReports(
            HttpServletRequest request,
            @RequestParam String mobile
    ) {
        internalRequestAuthService.authorizeIntegrationCall(request, mobile);

        List<Patient> patients = findPatientsByMobile(mobile);
        if (patients.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<ReportIntegrationDto> response = new ArrayList<>();
        for (Patient patient : patients) {
            String patientName = (patient.getFirstName() + " " + patient.getLastName()).trim();
            List<ServiceRequest> requests = serviceRequestRepository.findByPatient(patient);
            for (ServiceRequest req : requests) {
                // Skip cancelled or empty requests
                if ("cancelled".equalsIgnoreCase(req.getStatus())) {
                    continue;
                }

                ReportIntegrationDto dto = new ReportIntegrationDto();
                dto.serviceRequestId = req.getId();
                dto.encounterId = req.getEncounter() != null ? req.getEncounter().getId() : null;
                
                // If the encounter is APPROVED or COMPLETED, we show it as finalized
                // so PHR users can access the reports immediately.
                String displayStatus = req.getStatus();
                if (req.getEncounter() != null) {
                    String encounterStatus = req.getEncounter().getStatus();
                    if ("APPROVED".equalsIgnoreCase(encounterStatus) || "COMPLETED".equalsIgnoreCase(encounterStatus)) {
                        displayStatus = "completed";
                    }
                }
                
                dto.status = displayStatus;
                dto.localOrderValue = req.getLocalOrderValue();
                dto.createdAt = req.getCreatedAt() != null ? req.getCreatedAt().toString() : "";
                dto.patientName = patientName;
                dto.relationship = patient.getRelationship();
                dto.isDependent = patient.getIsDependent();
                DiagnosticReport report = diagnosticReportRepository.findByServiceRequest_Id(req.getId()).orElse(null);
                dto.reportGcsUrl = report != null ? report.getReportGcsUrl() : null;

                // Fetch test names via ServiceRequestItem join table
                List<ServiceRequestItem> items = serviceRequestItemRepository.findByServiceRequest(req);
                if (items.isEmpty()) continue; // Skip requests with no tests

                dto.tests = items.stream()
                        .filter(item -> item.getTest() != null)
                        .map(item -> item.getTest().getTestName())
                        .collect(Collectors.toList());

                response.add(dto);
            }
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/{id}")
    public ResponseEntity<ReportDetailsIntegrationDto> getReportDetails(
            HttpServletRequest request,
            @PathVariable Integer id,
            @RequestParam String mobile
    ) {
        internalRequestAuthService.authorizeIntegrationCall(request, mobile);

        ServiceRequest req = serviceRequestRepository.findById(id).orElse(null);
        if (req == null) {
            return ResponseEntity.notFound().build();
        }
        if (!canAccessServiceRequest(req, mobile)) {
            return ResponseEntity.notFound().build();
        }

        ReportDetailsIntegrationDto dto = new ReportDetailsIntegrationDto();
        dto.serviceRequestId = req.getId();
        dto.localOrderValue = req.getLocalOrderValue();
        
        String displayStatus = req.getStatus();
        if (req.getEncounter() != null) {
            String encounterStatus = req.getEncounter().getStatus();
            if ("APPROVED".equalsIgnoreCase(encounterStatus) || "COMPLETED".equalsIgnoreCase(encounterStatus)) {
                displayStatus = "completed";
            }
        }
        dto.status = displayStatus;
        dto.createdAt = req.getCreatedAt() != null ? req.getCreatedAt().toString() : "";

        if (req.getPatient() != null) {
            dto.patientName = (req.getPatient().getFirstName() + " " + req.getPatient().getLastName()).trim();
        }

        DiagnosticReport report = diagnosticReportRepository.findByServiceRequest_Id(req.getId()).orElse(null);
        dto.reportGcsUrl = report != null ? report.getReportGcsUrl() : null;

        if (req.getEncounter() != null && req.getEncounter().getServiceProvider() != null) {
            dto.labName = req.getEncounter().getServiceProvider().getOrganizationName();
        }

        // Fetch analytes/observations
        List<com.halo.lims.model.Observation> observations = observationRepository.findByServiceRequestId(id);
        dto.analytes = observations.stream().map(obs -> {
            AnalyteIntegrationDto analyte = new AnalyteIntegrationDto();
            analyte.name = obs.getAnalyte() != null ? obs.getAnalyte().getAnalyteName() : "Unknown";

            analyte.result = obs.getValueNumeric() != null ? obs.getValueNumeric().toString() : obs.getValueString();
            analyte.unit = obs.getUnit() != null ? obs.getUnit().getName() : "";
            analyte.referenceRange = obs.getReferenceRange() != null ? obs.getReferenceRange().getTextRange() : "";
            
            // Robust status color logic
            analyte.statusColor = "GREEN";
            if (obs.getValueNumeric() != null && obs.getReferenceRange() != null) {
                Double val = obs.getValueNumeric().doubleValue();
                Double low = obs.getReferenceRange().getLowValue() != null ? obs.getReferenceRange().getLowValue().doubleValue() : null;
                Double high = obs.getReferenceRange().getHighValue() != null ? obs.getReferenceRange().getHighValue().doubleValue() : null;

                if (low != null && val < low) {
                    analyte.statusColor = "RED";
                } else if (high != null && val > high) {
                    analyte.statusColor = "RED";
                } else if (low != null && high != null) {
                    Double span = high - low;
                    if (span > 0) {
                        Double band = span * 0.1;
                        if (val <= (low + band) || val >= (high - band)) {
                            analyte.statusColor = "AMBER";
                        }
                    }
                }
            }
            if ("GREEN".equals(analyte.statusColor) && obs.getInterpretationCode() != null) {
                String code = obs.getInterpretationCode().trim().toUpperCase();
                if (code.equals("H") || code.equals("HH") || code.equals("L") || code.equals("LL") || code.equals("A") || code.contains("ABNORMAL") || code.contains("POSITIVE") || code.contains("REACTIVE") || code.equals("POS") || code.equals("R")) {
                    analyte.statusColor = "RED";
                } else if (code.contains("BORDERLINE") || code.contains("WARN") || code.contains("AMBER")) {
                    analyte.statusColor = "AMBER";
                }
            }
            return analyte;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/reports/{id}/pdf")
    public ResponseEntity<byte[]> getReportPdf(
            HttpServletRequest request,
            @PathVariable Integer id,
            @RequestParam String mobile,
            @RequestParam(defaultValue = "true") boolean withHeader,
            @RequestParam(defaultValue = "regular") String reportType
    ) {
        internalRequestAuthService.authorizeIntegrationCall(request, mobile);

        ServiceRequest req = serviceRequestRepository.findById(id).orElse(null);
        if (req == null || !canAccessServiceRequest(req, mobile)) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdfBytes;
        try {
            pdfBytes = reportService.getStoredOrGeneratedPdfReport(id, withHeader, reportType);
        } catch (IllegalStateException ex) {
            // Report generation or template rendering failure — surface the full cause
            String detail = ex.getMessage();
            if (ex.getCause() != null && ex.getCause().getMessage() != null) {
                detail += " | root cause: " + ex.getCause().getMessage();
            }
            log.warn("Report PDF generation failed for serviceRequest={}: {}", id, detail, ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, detail, ex);
        } catch (RuntimeException ex) {
            // Unexpected error — log the full stack trace so we can see the real problem
            log.error("Unexpected error generating PDF for serviceRequest={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "PDF generation failed: " + ex.getMessage(), ex);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = reportService.resolvePdfFileName(id, reportType);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
        headers.setCacheControl(CacheControl.noStore().mustRevalidate().cachePrivate());
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/bills")
    public ResponseEntity<List<BillIntegrationDto>> getBills(
            HttpServletRequest request,
            @RequestParam String mobile
    ) {
        internalRequestAuthService.authorizeIntegrationCall(request, mobile);

        List<Patient> patients = findPatientsByMobile(mobile);
        if (patients.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<BillIntegrationDto> response = new ArrayList<>();
        for (Patient patient : patients) {
            String patientName = (patient.getFirstName() + " " + patient.getLastName()).trim();
            List<Bill> bills = billRepository.findByPatient(patient);
            for (Bill bill : bills) {
                BillIntegrationDto dto = new BillIntegrationDto();
                dto.billId = bill.getId();
                dto.invoiceNumber = bill.getInvoiceNumber();
                dto.status = bill.getStatus();
                dto.netAmount = bill.getNetAmount();
                dto.amountPaid = bill.getPaidAmount();
                dto.amountDue = bill.getDueAmount();
                dto.totalAmount = bill.getTotalAmount();
                dto.discountAmount = bill.getDiscountAmount();
                dto.paymentMethod = bill.getPaymentMethod();
                dto.invoiceDate = bill.getInvoiceDate() != null ? bill.getInvoiceDate().toString() : "";
                dto.patientName = patientName;
                response.add(dto);
            }
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analyte-history")
    public ResponseEntity<Map<String, Object>> getAnalyteHistory(
            HttpServletRequest request,
            @RequestParam String mobile
    ) {
        internalRequestAuthService.authorizeIntegrationCall(request, mobile);

        List<Patient> patients = findPatientsByMobile(mobile);
        if (patients.isEmpty()) {
            return ResponseEntity.ok(Map.of("tests", new ArrayList<>()));
        }

        List<com.halo.lims.model.Observation> observations = observationRepository.findByPatientInOrderByEffectiveDateTimeDesc(patients);

        Map<String, List<com.halo.lims.model.Observation>> byAnalyte = new LinkedHashMap<>();
        for (com.halo.lims.model.Observation obs : observations) {
            if (obs.getAnalyte() == null || obs.getValueNumeric() == null || obs.getStatus().equalsIgnoreCase("cancelled")) continue;
            String analyteName = obs.getAnalyte().getAnalyteName();
            byAnalyte.computeIfAbsent(analyteName, k -> new ArrayList<>()).add(obs);
        }

        List<Map<String, Object>> analytesList = new ArrayList<>();
        for (Map.Entry<String, List<com.halo.lims.model.Observation>> entry : byAnalyte.entrySet()) {
            List<com.halo.lims.model.Observation> history = entry.getValue();
            if (history.isEmpty()) continue;
            
            com.halo.lims.model.Observation currentObs = history.get(0);
            
            List<Map<String, Object>> historyList = new ArrayList<>();
            for (com.halo.lims.model.Observation obs : history) {
                Map<String, Object> h = new LinkedHashMap<>();
                h.put("date", obs.getEffectiveDateTime() != null ? obs.getEffectiveDateTime().toLocalDate().toString() : "");
                h.put("value", obs.getValueNumeric().toString());
                
                String statusColor = "normal";
                if (obs.getReferenceRange() != null) {
                    Double val = obs.getValueNumeric().doubleValue();
                    Double low = obs.getReferenceRange().getLowValue() != null ? obs.getReferenceRange().getLowValue().doubleValue() : null;
                    Double high = obs.getReferenceRange().getHighValue() != null ? obs.getReferenceRange().getHighValue().doubleValue() : null;
                    if (low != null && val < low) {
                        statusColor = "low";
                    } else if (high != null && val > high) {
                        statusColor = "high";
                    }
                }
                if ("normal".equals(statusColor) && obs.getInterpretationCode() != null) {
                    String code = obs.getInterpretationCode().trim().toUpperCase();
                    if (code.equals("L") || code.equals("LL")) {
                        statusColor = "low";
                    } else if (code.equals("H") || code.equals("HH") || code.equals("A") || code.contains("ABNORMAL")) {
                        statusColor = "high";
                    }
                }
                h.put("status", statusColor);
                historyList.add(h);
            }
            
            String currentStatusColor = "GREEN";
            String trend = "stable";
            String prevValue = null;
            String prevDate = null;
            
            if (history.size() > 1) {
                com.halo.lims.model.Observation prevObs = history.get(1);
                prevValue = prevObs.getValueNumeric().toString();
                prevDate = prevObs.getEffectiveDateTime() != null ? prevObs.getEffectiveDateTime().toLocalDate().toString() : "";
                
                Double curr = currentObs.getValueNumeric().doubleValue();
                Double prev = prevObs.getValueNumeric().doubleValue();
                if (curr > prev) trend = "up";
                else if (curr < prev) trend = "down";
            }
            
            if (currentObs.getReferenceRange() != null) {
                Double val = currentObs.getValueNumeric().doubleValue();
                Double low = currentObs.getReferenceRange().getLowValue() != null ? currentObs.getReferenceRange().getLowValue().doubleValue() : null;
                Double high = currentObs.getReferenceRange().getHighValue() != null ? currentObs.getReferenceRange().getHighValue().doubleValue() : null;
                if (low != null && val < low) {
                    currentStatusColor = "RED";
                } else if (high != null && val > high) {
                    currentStatusColor = "RED";
                } else if (low != null && high != null) {
                    Double span = high - low;
                    if (span > 0) {
                        Double band = span * 0.1;
                        if (val <= (low + band) || val >= (high - band)) {
                            currentStatusColor = "AMBER";
                        }
                    }
                }
            }
            if ("GREEN".equals(currentStatusColor) && currentObs.getInterpretationCode() != null) {
                String code = currentObs.getInterpretationCode().trim().toUpperCase();
                if (code.equals("H") || code.equals("HH") || code.equals("L") || code.equals("LL") || code.equals("A") || code.contains("ABNORMAL") || code.contains("POSITIVE") || code.contains("REACTIVE") || code.equals("POS") || code.equals("R")) {
                    currentStatusColor = "RED";
                } else if (code.contains("BORDERLINE") || code.contains("WARN") || code.contains("AMBER")) {
                    currentStatusColor = "AMBER";
                }
            }

            Map<String, Object> analyteMap = new LinkedHashMap<>();
            analyteMap.put("name", entry.getKey());
            analyteMap.put("unit", currentObs.getUnit() != null ? currentObs.getUnit().getName() : "");
            analyteMap.put("reference_range", currentObs.getReferenceRange() != null ? currentObs.getReferenceRange().getTextRange() : "");
            analyteMap.put("current_value", currentObs.getValueNumeric().toString());
            analyteMap.put("previous_value", prevValue);
            analyteMap.put("current_date", currentObs.getEffectiveDateTime() != null ? currentObs.getEffectiveDateTime().toLocalDate().toString() : "");
            analyteMap.put("previous_date", prevDate);
            analyteMap.put("status_color", currentStatusColor);
            analyteMap.put("trend", trend);
            analyteMap.put("history", historyList);
            
            analytesList.add(analyteMap);
        }
        
        Map<String, Object> testMap = new LinkedHashMap<>();
        testMap.put("test_name", "All Biomarkers");
        testMap.put("analytes", analytesList);
        
        return ResponseEntity.ok(Map.of("tests", List.of(testMap)));
    }

    private List<Patient> findPatientsByMobile(String mobile) {
        String normalizedMobile = Patient.normalizePhone(mobile);
        if (normalizedMobile == null) {
            return new ArrayList<>();
        }

        // Fast path: canonical normalized phone lookup.
        List<Patient> patients = patientRepository.findByContactPhoneNormalized(normalizedMobile);
        if (!patients.isEmpty()) {
            return patients;
        }

        // Compatibility fallback for legacy seed/manual SQL data where contact_phone_normalized
        // was not populated but encrypted contact_phone exists.
        List<Patient> fallbackMatches = patientRepository.findAll().stream()
                .filter(patient -> normalizedMobile.equals(Patient.normalizePhone(patient.getContactPhone())))
                .collect(Collectors.toList());

        if (!fallbackMatches.isEmpty()) {
            log.info("PHR integration fallback mobile match used for mobile ending with {} (matched {} patient(s))",
                    normalizedMobile,
                    fallbackMatches.size());
        }

        return fallbackMatches;
    }

    private boolean canAccessServiceRequest(ServiceRequest serviceRequest, String mobile) {
        if (serviceRequest == null || serviceRequest.getPatient() == null) {
            return false;
        }

        List<Patient> matchedPatients = findPatientsByMobile(mobile);
        Integer patientId = serviceRequest.getPatient().getId();
        return matchedPatients.stream()
                .map(Patient::getId)
                .anyMatch(patientId::equals);
    }

    static class ReportIntegrationDto {
        public Integer serviceRequestId;
        public Integer encounterId;
        public String status;
        public String localOrderValue;
        public String createdAt;
        public List<String> tests;
        public String patientName;
        public String relationship;
        public Boolean isDependent;
        public String reportGcsUrl;
    }

    static class ReportDetailsIntegrationDto {
        public Integer serviceRequestId;
        public String status;
        public String localOrderValue;
        public String createdAt;
        public String patientName;
        public String labName;
        public String reportGcsUrl;
        public List<AnalyteIntegrationDto> analytes;
    }

    static class AnalyteIntegrationDto {
        public String name;
        public String result;
        public String unit;
        public String referenceRange;
        public String statusColor;
    }

    static class BillIntegrationDto {
        public Integer billId;
        public String invoiceNumber;
        public String status;
        public java.math.BigDecimal totalAmount;
        public java.math.BigDecimal discountAmount;
        public java.math.BigDecimal netAmount;
        public java.math.BigDecimal amountPaid;
        public java.math.BigDecimal amountDue;

        public String paymentMethod;
        public String invoiceDate;
        public String patientName;
    }
}

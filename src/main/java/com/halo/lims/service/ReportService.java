package com.halo.lims.service;

import com.halo.lims.dto.report.*;
import com.halo.lims.dto.report.ReportPdfDeletionResponse;
import com.halo.lims.model.*;
import com.halo.lims.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Facade service for report operations.
 * Delegates to focused services:
 * - {@link ReportApprovalService} — approval status checks
 * - {@link ReportDtoBuilder} — DTO construction
 * - {@link ReportRenderer} — HTML/PDF rendering
 * - {@link ReportCacheService} — PDF caching
 * - {@link ReportImageService} — QR/sparkline generation
 */
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private static final String REPORT_TYPE_REGULAR = "regular";
    private static final String REPORT_TYPE_SMART = "smart";
    private static final Pattern SAFE_FILENAME_CHAR_PATTERN = Pattern.compile("[^a-zA-Z0-9._-]+");

    private final ServiceRequestRepository serviceRequestRepository;
    private final DiagnosticReportRepository diagnosticReportRepository;
    private final ObservationRepository observationRepository;
    private final ReportApprovalService reportApprovalService;
    private final ReportDtoBuilder reportDtoBuilder;
    private final ReportRenderer reportRenderer;
    private final ReportCacheService reportCacheService;
    private final ReportStorageService reportStorageService;
    private final IdentifierGenerationService identifierGenerationService;

    public ReportService(
            ServiceRequestRepository serviceRequestRepository,
            DiagnosticReportRepository diagnosticReportRepository,
            ObservationRepository observationRepository,
            ReportApprovalService reportApprovalService,
            ReportDtoBuilder reportDtoBuilder,
            ReportRenderer reportRenderer,
            ReportCacheService reportCacheService,
            ReportStorageService reportStorageService,
            IdentifierGenerationService identifierGenerationService) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.diagnosticReportRepository = diagnosticReportRepository;
        this.observationRepository = observationRepository;
        this.reportApprovalService = reportApprovalService;
        this.reportDtoBuilder = reportDtoBuilder;
        this.reportRenderer = reportRenderer;
        this.reportCacheService = reportCacheService;
        this.reportStorageService = reportStorageService;
        this.identifierGenerationService = identifierGenerationService;
    }

    // -------------------------------------------------------------------------
    // Approval status — delegates to ReportApprovalService
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ReportApprovalStatusResponse getReportApprovalStatus(Integer serviceRequestId) {
        ReportApprovalStatusResponse response = reportApprovalService.getReportApprovalStatus(serviceRequestId);
        
        // Enrich with staleness info
        diagnosticReportRepository.findByServiceRequest_Id(serviceRequestId).ifPresent(report -> {
            if (report.getReportGcsUrl() != null) {
                OffsetDateTime clinicalLastModified = getClinicalLastModified(serviceRequestId);
                boolean isStale = report.getUpdatedAt().isBefore(clinicalLastModified);
                response.setMessage(response.getMessage() + (isStale ? " (Updates available)" : " (Cached)"));
            }
        });
        
        return response;
    }

    private OffsetDateTime getClinicalLastModified(Integer serviceRequestId) {
        ServiceRequest sr = serviceRequestRepository.findById(serviceRequestId).orElse(null);
        if (sr == null) return OffsetDateTime.MIN;

        OffsetDateTime obsMax = observationRepository.findMaxUpdatedAtByServiceRequestId(serviceRequestId);
        OffsetDateTime patientMax = sr.getPatient() != null ? sr.getPatient().getUpdatedAt() : null;

        OffsetDateTime latest = obsMax != null ? obsMax : OffsetDateTime.MIN;
        if (patientMax != null && patientMax.isAfter(latest)) {
            latest = patientMax;
        }
        return latest;
    }

    // -------------------------------------------------------------------------
    // PDF generation — with caching support
    // -------------------------------------------------------------------------

    @Transactional
    public byte[] getStoredOrGeneratedPdfReport(Integer serviceRequestId, boolean withHeader) {
        return getStoredOrGeneratedPdfReport(serviceRequestId, withHeader, REPORT_TYPE_REGULAR);
    }

    @Transactional
    public byte[] getStoredOrGeneratedPdfReport(Integer serviceRequestId, boolean withHeader, String reportType) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new RuntimeException("Service Request not found with ID: " + serviceRequestId));

        String normalizedReportType = normalizeReportType(reportType);
        ReportApprovalStatusResponse approvalStatus = getReportApprovalStatus(serviceRequestId);
        if (!approvalStatus.isReady()) {
            throw new IllegalStateException(approvalStatus.getMessage());
        }

        DiagnosticReport report = diagnosticReportRepository.findByServiceRequest_Id(serviceRequestId).orElse(null);
        OffsetDateTime clinicalLastModified = getClinicalLastModified(serviceRequestId);
        
        boolean isFresh = report != null 
                && report.getReportGcsUrl() != null 
                && storedReferenceMatchesType(report.getReportGcsUrl(), normalizedReportType)
                && report.getUpdatedAt() != null 
                && report.getUpdatedAt().isAfter(clinicalLastModified);

        // If fresh and not forced, attempt to serve from storage
        if (isFresh) {
            try {
                return reportStorageService.downloadFile(report.getReportGcsUrl());
            } catch (Exception e) {
                log.warn("Failed to download cached report for SR {}, regenerating...", serviceRequestId, e);
            }
        }

        // Otherwise, generate, upload and persist
        byte[] pdf = buildUnifiedPdfReport(serviceRequestId, withHeader, normalizedReportType);
        
        if (report == null) {
            report = new DiagnosticReport();
            report.setServiceRequest(serviceRequest);
            report.setPatient(serviceRequest.getPatient());
            report.setEncounter(serviceRequest.getEncounter());
            report.setStatus("final");
            report.setEffectiveDateTime(OffsetDateTime.now());
            report.setIssuedDateTime(OffsetDateTime.now());
            report.setLocalReportSystem("local://reports");
            report.setLocalReportValue(identifierGenerationService.generateReportValue(serviceRequest.getPatient().getOrganization().getId(), 3));
        }

        String objectName = String.format("reports/%s/%s_%s.pdf", 
                serviceRequest.getPatient().getOrganization().getId(),
                report.getLocalReportValue(),
                normalizedReportType);
        
        try {
            String reportReference = reportStorageService.uploadFile(objectName, pdf, "application/pdf");
            report.setReportGcsUrl(reportReference);
            report.setUpdatedAt(OffsetDateTime.now());
            diagnosticReportRepository.save(report);
        } catch (Exception ex) {
            log.warn(
                    "Failed to cache generated {} report for service request {}, returning the fresh PDF directly.",
                    normalizedReportType,
                    serviceRequestId,
                    ex
            );
        }

        return pdf;
    }

    @Transactional
    public byte[] getStoredOrGeneratedPdfReportByLocalReportValue(String localReportValue, boolean withHeader) {
        return getStoredOrGeneratedPdfReportByLocalReportValue(localReportValue, withHeader, REPORT_TYPE_REGULAR);
    }

    @Transactional
    public byte[] getStoredOrGeneratedPdfReportByLocalReportValue(String localReportValue, boolean withHeader, String reportType) {
        if (localReportValue == null || localReportValue.isBlank()) {
            throw new IllegalArgumentException("Local report value must not be blank");
        }
        DiagnosticReport diagnosticReport = diagnosticReportRepository.findByLocalReportValue(localReportValue)
                .orElseThrow(() -> new RuntimeException("Diagnostic report not found for value: " + localReportValue));
        return getStoredOrGeneratedPdfReport(diagnosticReport.getServiceRequest().getId(), withHeader, reportType);
    }

    @Transactional
    public ReportPdfDeletionResponse deleteStoredPdfReport(Integer serviceRequestId) {
        DiagnosticReport diagnosticReport = diagnosticReportRepository.findByServiceRequest_Id(serviceRequestId)
                .orElseThrow(() -> new RuntimeException("Diagnostic report not found for service request: " + serviceRequestId));

        String storedReference = diagnosticReport.getReportGcsUrl();
        if (storedReference == null || storedReference.isBlank()) {
            storedReference = buildFallbackStoredReference(diagnosticReport);
        }

        if (storedReference == null || storedReference.isBlank()) {
            throw new IllegalStateException("No stored PDF reference found for service request: " + serviceRequestId);
        }

        reportStorageService.deleteFile(storedReference);
        diagnosticReport.setReportGcsUrl(null);
        diagnosticReportRepository.save(diagnosticReport);

        return new ReportPdfDeletionResponse(
                serviceRequestId,
                true,
                storedReference,
                "Deleted stored PDF reference for service request " + serviceRequestId
        );
    }

    @Transactional(readOnly = true)
    public String resolvePdfFileName(Integer serviceRequestId) {
        return resolvePdfFileName(serviceRequestId, REPORT_TYPE_REGULAR);
    }

    @Transactional(readOnly = true)
    public String resolvePdfFileName(Integer serviceRequestId, String reportType) {
        return buildReportDownloadFileStemForServiceRequest(serviceRequestId, reportType) + ".pdf";
    }

    @Transactional(readOnly = true)
    public String resolveHtmlFileName(Integer serviceRequestId, String reportType) {
        return buildReportDownloadFileStemForServiceRequest(serviceRequestId, reportType) + ".html";
    }

    @Transactional(readOnly = true)
    public String resolvePdfFileNameByLocalReportValue(String localReportValue) {
        return resolvePdfFileNameByLocalReportValue(localReportValue, REPORT_TYPE_REGULAR);
    }

    @Transactional(readOnly = true)
    public String resolvePdfFileNameByLocalReportValue(String localReportValue, String reportType) {
        if (localReportValue == null || localReportValue.isBlank()) {
            return "report-unknown-" + normalizeReportType(reportType) + ".pdf";
        }

        Optional<DiagnosticReport> reportOptional = diagnosticReportRepository.findByLocalReportValue(localReportValue);
        if (reportOptional.isEmpty()) {
            return "report-" + sanitizeFileNamePart(localReportValue) + "-" + normalizeReportType(reportType) + ".pdf";
        }

        DiagnosticReport report = reportOptional.get();
        Integer serviceRequestId = report.getServiceRequest() != null ? report.getServiceRequest().getId() : null;
        if (serviceRequestId == null) {
            return "report-" + sanitizeFileNamePart(localReportValue) + "-" + normalizeReportType(reportType) + ".pdf";
        }

        return resolvePdfFileName(serviceRequestId, reportType);
    }

    @Transactional(readOnly = true)
    public String resolveReportDownloadStem(Integer serviceRequestId, String reportType) {
        return buildReportDownloadFileStemForServiceRequest(serviceRequestId, reportType);
    }

    @Transactional(readOnly = true)
    public String resolveReportDownloadStemByLocalReportValue(String localReportValue, String reportType) {
        if (localReportValue == null || localReportValue.isBlank()) {
            return "report-unknown-" + normalizeReportType(reportType);
        }

        return diagnosticReportRepository.findByLocalReportValue(localReportValue)
                .map(DiagnosticReport::getServiceRequest)
                .map(ServiceRequest::getId)
                .map(serviceRequestId -> buildReportDownloadFileStemForServiceRequest(serviceRequestId, reportType))
                .orElse("report-" + sanitizeFileNamePart(localReportValue) + "-" + normalizeReportType(reportType));
    }

    @Transactional(readOnly = true)
    public String resolvePdfFileNameLegacy(Integer serviceRequestId) {
        return diagnosticReportRepository.findByServiceRequest_Id(serviceRequestId)
                .map(DiagnosticReport::getLocalReportValue)
                .filter(v -> v != null && !v.isBlank())
                .map(v -> v + ".pdf")
                .orElse("report-" + serviceRequestId + ".pdf");
    }

    @Transactional(readOnly = true)
    public String resolvePdfFileNameByLocalReportValueLegacy(String localReportValue) {
        return diagnosticReportRepository.findByLocalReportValue(localReportValue)
                .map(DiagnosticReport::getLocalReportValue)
                .filter(v -> v != null && !v.isBlank())
                .map(v -> v + ".pdf")
                .orElse("report-" + localReportValue + ".pdf");
    }

    // -------------------------------------------------------------------------
    // HTML + PDF building — delegates to ReportDtoBuilder + ReportRenderer
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public String buildUnifiedHtmlReport(Integer serviceRequestId, boolean withHeader) {
        return buildUnifiedHtmlReport(serviceRequestId, withHeader, REPORT_TYPE_REGULAR);
    }

    @Transactional(readOnly = true)
    public String buildUnifiedHtmlReport(Integer serviceRequestId, boolean withHeader, String reportType) {
        ReportApprovalStatusResponse approvalStatus = getReportApprovalStatus(serviceRequestId);
        if (!approvalStatus.isReady()) {
            throw new IllegalStateException(approvalStatus.getMessage());
        }
        DiagnosticReportDTO reportDto = buildReportDTO(serviceRequestId, withHeader,
                normalizeReportType(reportType), approvalStatus);
        return renderReportHtml(reportDto);
    }

    @Transactional(readOnly = true)
    public byte[] buildUnifiedPdfReport(Integer serviceRequestId, boolean withHeader) {
        return buildUnifiedPdfReport(serviceRequestId, withHeader, REPORT_TYPE_REGULAR);
    }

    @Transactional(readOnly = true)
    public byte[] buildUnifiedPdfReport(Integer serviceRequestId, boolean withHeader, String reportType) {
        ReportApprovalStatusResponse approvalStatus = getReportApprovalStatus(serviceRequestId);
        if (!approvalStatus.isReady()) {
            throw new IllegalStateException(approvalStatus.getMessage());
        }
        DiagnosticReportDTO reportDto = buildReportDTO(serviceRequestId, withHeader,
                normalizeReportType(reportType), approvalStatus);
        return renderReportPdf(reportDto);
    }

    // -------------------------------------------------------------------------
    // Delegate methods — maintain backward compatibility for controllers
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public DiagnosticReportDTO buildReportDTO(Integer serviceRequestId, boolean withHeader,
                                              String normalizedReportType,
                                              ReportApprovalStatusResponse approvalStatus) {
        return reportDtoBuilder.buildReportDTO(serviceRequestId, withHeader, normalizedReportType, approvalStatus);
    }

    @Transactional(readOnly = true)
    public String renderReportHtml(DiagnosticReportDTO reportDto) {
        return reportRenderer.renderReportHtml(reportDto);
    }

    @Transactional(readOnly = true)
    public byte[] renderReportPdf(DiagnosticReportDTO reportDto) {
        return reportRenderer.renderReportPdf(reportDto);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String normalizeReportType(String reportType) {
        if (reportType == null || reportType.isBlank()) return REPORT_TYPE_REGULAR;
        return REPORT_TYPE_SMART.equals(reportType.trim().toLowerCase(Locale.ENGLISH))
                ? REPORT_TYPE_SMART : REPORT_TYPE_REGULAR;
    }

    private boolean storedReferenceMatchesType(String storedReference, String reportType) {
        if (storedReference == null || storedReference.isBlank()) {
            return false;
        }
        String expectedSuffix = "_" + normalizeReportType(reportType) + ".pdf";
        return storedReference.toLowerCase(Locale.ENGLISH).contains(expectedSuffix);
    }

    private String buildFallbackStoredReference(DiagnosticReport diagnosticReport) {
        if (diagnosticReport.getPatient() == null
                || diagnosticReport.getPatient().getOrganization() == null
                || diagnosticReport.getPatient().getOrganization().getId() == null
                || diagnosticReport.getLocalReportValue() == null
                || diagnosticReport.getLocalReportValue().isBlank()) {
            return null;
        }

        return "local://reports/"
                + diagnosticReport.getPatient().getOrganization().getId()
                + "/"
                + diagnosticReport.getLocalReportValue()
                + ".pdf";
    }

    private String buildReportDownloadFileStemForServiceRequest(Integer serviceRequestId, String reportType) {
        String normalizedType = normalizeReportType(reportType);

        Optional<ServiceRequest> serviceRequestOptional = serviceRequestRepository.findById(serviceRequestId);
        if (serviceRequestOptional.isEmpty()) {
            return "report-" + serviceRequestId + "-" + normalizedType;
        }

        ServiceRequest serviceRequest = serviceRequestOptional.get();
        Patient patient = serviceRequest.getPatient();
        Organization organization = patient != null ? patient.getOrganization() : null;

        String orgName = organization != null ? organization.getOrganizationName() : "org";
        String patientName = patient != null
                ? ((safe(patient.getFirstName()) + " " + safe(patient.getLastName())).trim())
                : "patient";
        String registrationId = patient != null && patient.getLocalMrnValue() != null && !patient.getLocalMrnValue().isBlank()
                ? patient.getLocalMrnValue()
                : "reg" + serviceRequestId;

        return sanitizeFileNamePart(orgName)
                + "_"
                + sanitizeFileNamePart(patientName)
                + "_"
                + sanitizeFileNamePart(registrationId)
                + "_"
                + normalizedType;
    }

    private String sanitizeFileNamePart(String value) {
        if (value == null || value.isBlank()) {
            return "na";
        }
        String normalized = SAFE_FILENAME_CHAR_PATTERN.matcher(value.trim()).replaceAll("_");
        normalized = normalized.replaceAll("_+", "_");
        normalized = normalized.replaceAll("^_+", "");
        normalized = normalized.replaceAll("_+$", "");
        return normalized.isBlank() ? "na" : normalized;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}

package com.halo.lims.controller;

import com.halo.lims.dto.report.DiagnosticReportDTO;
import com.halo.lims.dto.report.ReportApprovalStatusResponse;
import com.halo.lims.dto.report.ReportPdfDeletionResponse;
import com.halo.lims.service.HashidService;
import com.halo.lims.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final HashidService hashidService;
    private final boolean reportDeleteEnabled;

    public ReportController(ReportService reportService,
                            HashidService hashidService,
                            @Value("${app.report.delete.enabled:false}") boolean reportDeleteEnabled) {
        this.reportService = reportService;
        this.hashidService = hashidService;
        this.reportDeleteEnabled = reportDeleteEnabled;
    }

    @GetMapping("/approval-status/{serviceRequestId:[0-9]+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATHOLOGIST', 'TECHNICIAN', 'MANAGER') and @securityService.canAccessServiceRequest(#serviceRequestId)")
    public ResponseEntity<ReportApprovalStatusResponse> getReportApprovalStatus(@PathVariable Integer serviceRequestId) {
        ReportApprovalStatusResponse status = reportService.getReportApprovalStatus(serviceRequestId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/download/{serviceRequestId:[0-9]+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATHOLOGIST', 'TECHNICIAN', 'MANAGER') and @securityService.canAccessServiceRequest(#serviceRequestId)")
    public ResponseEntity<byte[]> downloadUnifiedReport(
            @PathVariable Integer serviceRequestId,
            @RequestParam(defaultValue = "true") boolean withHeader,
            @RequestParam(defaultValue = "smart") String reportType // UPDATED: Default to Smart UI
    ) {
        ReportApprovalStatusResponse status = reportService.getReportApprovalStatus(serviceRequestId);
        if (!status.isReady()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, status.getMessage());
        }

        String html = reportService.buildUnifiedHtmlReport(serviceRequestId, withHeader, reportType);
        String filename = reportService.resolveHtmlFileName(serviceRequestId, reportType);
        if (filename == null || filename.isBlank()) {
            filename = "unified-report-" + serviceRequestId + ".html";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        headers.setCacheControl(CacheControl.noStore().mustRevalidate().cachePrivate());
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return ResponseEntity.ok()
                .headers(headers)
                .body(html.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/pdf/{serviceRequestId:[0-9]+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATHOLOGIST', 'TECHNICIAN', 'MANAGER') and @securityService.canAccessServiceRequest(#serviceRequestId)")
    public ResponseEntity<byte[]> downloadUnifiedReportPdf(
            @PathVariable Integer serviceRequestId,
            @RequestParam(defaultValue = "true") boolean withHeader,
            @RequestParam(defaultValue = "smart") String reportType // UPDATED: Default to Smart UI
    ) {
        return generatePdfResponse(serviceRequestId, withHeader, reportType);
    }

    @GetMapping("/public/{hashid}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATHOLOGIST', 'TECHNICIAN', 'MANAGER', 'PATIENT')")
    public ResponseEntity<byte[]> downloadUnifiedReportPdfByHash(
            @PathVariable String hashid,
            @RequestParam(defaultValue = "true") boolean withHeader,
            @RequestParam(defaultValue = "smart") String reportType
    ) {
        Integer serviceRequestId = hashidService.decodeToInt(hashid);
        if (serviceRequestId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid report identifier.");
        }
        return generatePdfResponse(serviceRequestId, withHeader, reportType);
    }

    private ResponseEntity<byte[]> generatePdfResponse(Integer serviceRequestId, boolean withHeader, String reportType) {
        ReportApprovalStatusResponse status = reportService.getReportApprovalStatus(serviceRequestId);
        if (!status.isReady()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, status.getMessage());
        }

        byte[] pdf;
        if (withHeader) {
            pdf = reportService.getStoredOrGeneratedPdfReport(serviceRequestId, true, reportType);
        } else {
            DiagnosticReportDTO report = reportService.buildReportDTO(
                serviceRequestId,
                false,
                reportType,
                status
            );
            pdf = reportService.renderReportPdf(report);
        }
        String filename = reportService.resolvePdfFileName(serviceRequestId, reportType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
        headers.setCacheControl(CacheControl.noStore().mustRevalidate().cachePrivate());
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/pdf/by-value/{localReportValue}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATHOLOGIST', 'TECHNICIAN', 'MANAGER')")
    public ResponseEntity<byte[]> downloadUnifiedReportPdfByLocalReportValue(
            @PathVariable String localReportValue,
            @RequestParam(defaultValue = "true") boolean withHeader,
            @RequestParam(defaultValue = "smart") String reportType // UPDATED: Default to Smart UI
    ) {
        byte[] pdf = reportService.getStoredOrGeneratedPdfReportByLocalReportValue(localReportValue, withHeader, reportType);
        String filename = reportService.resolvePdfFileNameByLocalReportValue(localReportValue, reportType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
        headers.setCacheControl(CacheControl.noStore().mustRevalidate().cachePrivate());
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @DeleteMapping("/pdf/{serviceRequestId:[0-9]+}")
    @PreAuthorize("hasRole('ADMIN') and @securityService.canAccessServiceRequest(#serviceRequestId)")
    public ResponseEntity<ReportPdfDeletionResponse> deleteUnifiedReportPdf(@PathVariable Integer serviceRequestId) {
        if (!reportDeleteEnabled) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                    "Deleting stored report PDFs is disabled in this environment.");
        }

        return ResponseEntity.ok(reportService.deleteStoredPdfReport(serviceRequestId));
    }
}

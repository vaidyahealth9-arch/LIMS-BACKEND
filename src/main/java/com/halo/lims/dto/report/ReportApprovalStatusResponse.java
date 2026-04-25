package com.halo.lims.dto.report;

import java.time.OffsetDateTime;

public class ReportApprovalStatusResponse {
    private boolean ready;
    private String message;
    private String approvedDoctorName;
    private String approvedDoctorSignatureImage;
    private OffsetDateTime approvedAt;
    private String ulrNumber;
    private String accreditationScopeQrContent;
    private String reportIntegrityQrContent;
    private String reportStorageReference;
    private String reportLocalValue;
    private String reportPdfPath;
    private String reportHashId;

    public ReportApprovalStatusResponse() {}

    public ReportApprovalStatusResponse(boolean ready, String message, String approvedDoctorName, String approvedDoctorSignatureImage, OffsetDateTime approvedAt, String ulrNumber, String accreditationScopeQrContent, String reportIntegrityQrContent, String reportStorageReference, String reportLocalValue, String reportPdfPath, String reportHashId) {
        this.ready = ready;
        this.message = message;
        this.approvedDoctorName = approvedDoctorName;
        this.approvedDoctorSignatureImage = approvedDoctorSignatureImage;
        this.approvedAt = approvedAt;
        this.ulrNumber = ulrNumber;
        this.accreditationScopeQrContent = accreditationScopeQrContent;
        this.reportIntegrityQrContent = reportIntegrityQrContent;
        this.reportStorageReference = reportStorageReference;
        this.reportLocalValue = reportLocalValue;
        this.reportPdfPath = reportPdfPath;
        this.reportHashId = reportHashId;
    }

    public static ReportApprovalStatusResponseBuilder builder() {
        return new ReportApprovalStatusResponseBuilder();
    }

    // Getters and Setters
    public boolean isReady() { return ready; }
    public void setReady(boolean ready) { this.ready = ready; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getApprovedDoctorName() { return approvedDoctorName; }
    public void setApprovedDoctorName(String approvedDoctorName) { this.approvedDoctorName = approvedDoctorName; }

    public String getApprovedDoctorSignatureImage() { return approvedDoctorSignatureImage; }
    public void setApprovedDoctorSignatureImage(String approvedDoctorSignatureImage) { this.approvedDoctorSignatureImage = approvedDoctorSignatureImage; }

    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getUlrNumber() { return ulrNumber; }
    public void setUlrNumber(String ulrNumber) { this.ulrNumber = ulrNumber; }

    public String getAccreditationScopeQrContent() { return accreditationScopeQrContent; }
    public void setAccreditationScopeQrContent(String accreditationScopeQrContent) { this.accreditationScopeQrContent = accreditationScopeQrContent; }

    public String getReportIntegrityQrContent() { return reportIntegrityQrContent; }
    public void setReportIntegrityQrContent(String reportIntegrityQrContent) { this.reportIntegrityQrContent = reportIntegrityQrContent; }

    public String getReportStorageReference() { return reportStorageReference; }
    public void setReportStorageReference(String reportStorageReference) { this.reportStorageReference = reportStorageReference; }

    public String getReportLocalValue() { return reportLocalValue; }
    public void setReportLocalValue(String reportLocalValue) { this.reportLocalValue = reportLocalValue; }

    public String getReportPdfPath() { return reportPdfPath; }
    public void setReportPdfPath(String reportPdfPath) { this.reportPdfPath = reportPdfPath; }

    public String getReportHashId() { return reportHashId; }
    public void setReportHashId(String reportHashId) { this.reportHashId = reportHashId; }

    public static class ReportApprovalStatusResponseBuilder {
        private boolean ready;
        private String message;
        private String approvedDoctorName;
        private String approvedDoctorSignatureImage;
        private OffsetDateTime approvedAt;
        private String ulrNumber;
        private String accreditationScopeQrContent;
        private String reportIntegrityQrContent;
        private String reportStorageReference;
        private String reportLocalValue;
        private String reportPdfPath;
        private String reportHashId;

        public ReportApprovalStatusResponseBuilder ready(boolean ready) { this.ready = ready; return this; }
        public ReportApprovalStatusResponseBuilder message(String message) { this.message = message; return this; }
        public ReportApprovalStatusResponseBuilder approvedDoctorName(String approvedDoctorName) { this.approvedDoctorName = approvedDoctorName; return this; }
        public ReportApprovalStatusResponseBuilder approvedDoctorSignatureImage(String approvedDoctorSignatureImage) { this.approvedDoctorSignatureImage = approvedDoctorSignatureImage; return this; }
        public ReportApprovalStatusResponseBuilder approvedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; return this; }
        public ReportApprovalStatusResponseBuilder ulrNumber(String ulrNumber) { this.ulrNumber = ulrNumber; return this; }
        public ReportApprovalStatusResponseBuilder accreditationScopeQrContent(String accreditationScopeQrContent) { this.accreditationScopeQrContent = accreditationScopeQrContent; return this; }
        public ReportApprovalStatusResponseBuilder reportIntegrityQrContent(String reportIntegrityQrContent) { this.reportIntegrityQrContent = reportIntegrityQrContent; return this; }
        public ReportApprovalStatusResponseBuilder reportStorageReference(String reportStorageReference) { this.reportStorageReference = reportStorageReference; return this; }
        public ReportApprovalStatusResponseBuilder reportLocalValue(String reportLocalValue) { this.reportLocalValue = reportLocalValue; return this; }
        public ReportApprovalStatusResponseBuilder reportPdfPath(String reportPdfPath) { this.reportPdfPath = reportPdfPath; return this; }
        public ReportApprovalStatusResponseBuilder reportHashId(String reportHashId) { this.reportHashId = reportHashId; return this; }

        public ReportApprovalStatusResponse build() {
            return new ReportApprovalStatusResponse(ready, message, approvedDoctorName, approvedDoctorSignatureImage, approvedAt, ulrNumber, accreditationScopeQrContent, reportIntegrityQrContent, reportStorageReference, reportLocalValue, reportPdfPath, reportHashId);
        }
    }
}

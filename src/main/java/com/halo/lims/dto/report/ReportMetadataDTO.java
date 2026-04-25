package com.halo.lims.dto.report;

public class ReportMetadataDTO {
    private String ulrNumber;
    private String accreditationScopeQrImage;
    private String reportIntegrityQrImage;
    private String accreditationScopeQrContent;
    private String reportIntegrityQrContent;
    private String generatedAt;

    public ReportMetadataDTO() {}

    public ReportMetadataDTO(String ulrNumber, String accreditationScopeQrImage, String reportIntegrityQrImage, String accreditationScopeQrContent, String reportIntegrityQrContent, String generatedAt) {
        this.ulrNumber = ulrNumber;
        this.accreditationScopeQrImage = accreditationScopeQrImage;
        this.reportIntegrityQrImage = reportIntegrityQrImage;
        this.accreditationScopeQrContent = accreditationScopeQrContent;
        this.reportIntegrityQrContent = reportIntegrityQrContent;
        this.generatedAt = generatedAt;
    }

    public static ReportMetadataDTOBuilder builder() {
        return new ReportMetadataDTOBuilder();
    }

    // Getters and Setters
    public String getUlrNumber() { return ulrNumber; }
    public void setUlrNumber(String ulrNumber) { this.ulrNumber = ulrNumber; }

    public String getAccreditationScopeQrImage() { return accreditationScopeQrImage; }
    public void setAccreditationScopeQrImage(String accreditationScopeQrImage) { this.accreditationScopeQrImage = accreditationScopeQrImage; }

    public String getReportIntegrityQrImage() { return reportIntegrityQrImage; }
    public void setReportIntegrityQrImage(String reportIntegrityQrImage) { this.reportIntegrityQrImage = reportIntegrityQrImage; }

    public String getAccreditationScopeQrContent() { return accreditationScopeQrContent; }
    public void setAccreditationScopeQrContent(String accreditationScopeQrContent) { this.accreditationScopeQrContent = accreditationScopeQrContent; }

    public String getReportIntegrityQrContent() { return reportIntegrityQrContent; }
    public void setReportIntegrityQrContent(String reportIntegrityQrContent) { this.reportIntegrityQrContent = reportIntegrityQrContent; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }

    public static class ReportMetadataDTOBuilder {
        private String ulrNumber;
        private String accreditationScopeQrImage;
        private String reportIntegrityQrImage;
        private String accreditationScopeQrContent;
        private String reportIntegrityQrContent;
        private String generatedAt;

        public ReportMetadataDTOBuilder ulrNumber(String ulrNumber) { this.ulrNumber = ulrNumber; return this; }
        public ReportMetadataDTOBuilder accreditationScopeQrImage(String accreditationScopeQrImage) { this.accreditationScopeQrImage = accreditationScopeQrImage; return this; }
        public ReportMetadataDTOBuilder reportIntegrityQrImage(String reportIntegrityQrImage) { this.reportIntegrityQrImage = reportIntegrityQrImage; return this; }
        public ReportMetadataDTOBuilder accreditationScopeQrContent(String accreditationScopeQrContent) { this.accreditationScopeQrContent = accreditationScopeQrContent; return this; }
        public ReportMetadataDTOBuilder reportIntegrityQrContent(String reportIntegrityQrContent) { this.reportIntegrityQrContent = reportIntegrityQrContent; return this; }
        public ReportMetadataDTOBuilder generatedAt(String generatedAt) { this.generatedAt = generatedAt; return this; }

        public ReportMetadataDTO build() {
            return new ReportMetadataDTO(ulrNumber, accreditationScopeQrImage, reportIntegrityQrImage, accreditationScopeQrContent, reportIntegrityQrContent, generatedAt);
        }
    }
}
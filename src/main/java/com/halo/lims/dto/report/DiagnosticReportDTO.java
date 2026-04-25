package com.halo.lims.dto.report;

import java.util.List;

public class DiagnosticReportDTO {
    private String reportType;
    private String reportTitle;
    private PatientDetails patient;
    private List<TestGroup> testGroups;
    private DoctorSignature signature;
    private AnalyticsSummary analytics;
    private List<LongitudinalTrend> trends;
    private List<String> insights;
    private ReportMetadataDTO metadata;
    private BrandingDTO branding;
    private boolean withHeader;

    public DiagnosticReportDTO() {}

    public DiagnosticReportDTO(String reportType, String reportTitle, PatientDetails patient, List<TestGroup> testGroups, DoctorSignature signature, AnalyticsSummary analytics, List<LongitudinalTrend> trends, List<String> insights, ReportMetadataDTO metadata, BrandingDTO branding, boolean withHeader) {
        this.reportType = reportType;
        this.reportTitle = reportTitle;
        this.patient = patient;
        this.testGroups = testGroups;
        this.signature = signature;
        this.analytics = analytics;
        this.trends = trends;
        this.insights = insights;
        this.metadata = metadata;
        this.branding = branding;
        this.withHeader = withHeader;
    }

    public static DiagnosticReportDTOBuilder builder() {
        return new DiagnosticReportDTOBuilder();
    }

    // Getters and Setters
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public PatientDetails getPatient() { return patient; }
    public void setPatient(PatientDetails patient) { this.patient = patient; }

    public List<TestGroup> getTestGroups() { return testGroups; }
    public void setTestGroups(List<TestGroup> testGroups) { this.testGroups = testGroups; }

    public DoctorSignature getSignature() { return signature; }
    public void setSignature(DoctorSignature signature) { this.signature = signature; }

    public AnalyticsSummary getAnalytics() { return analytics; }
    public void setAnalytics(AnalyticsSummary analytics) { this.analytics = analytics; }

    public List<LongitudinalTrend> getTrends() { return trends; }
    public void setTrends(List<LongitudinalTrend> trends) { this.trends = trends; }

    public List<String> getInsights() { return insights; }
    public void setInsights(List<String> insights) { this.insights = insights; }

    public ReportMetadataDTO getMetadata() { return metadata; }
    public void setMetadata(ReportMetadataDTO metadata) { this.metadata = metadata; }

    public BrandingDTO getBranding() { return branding; }
    public void setBranding(BrandingDTO branding) { this.branding = branding; }

    public boolean isWithHeader() { return withHeader; }
    public void setWithHeader(boolean withHeader) { this.withHeader = withHeader; }

    public static class DiagnosticReportDTOBuilder {
        private String reportType;
        private String reportTitle;
        private PatientDetails patient;
        private List<TestGroup> testGroups;
        private DoctorSignature signature;
        private AnalyticsSummary analytics;
        private List<LongitudinalTrend> trends;
        private List<String> insights;
        private ReportMetadataDTO metadata;
        private BrandingDTO branding;
        private boolean withHeader;

        public DiagnosticReportDTOBuilder reportType(String reportType) { this.reportType = reportType; return this; }
        public DiagnosticReportDTOBuilder reportTitle(String reportTitle) { this.reportTitle = reportTitle; return this; }
        public DiagnosticReportDTOBuilder patient(PatientDetails patient) { this.patient = patient; return this; }
        public DiagnosticReportDTOBuilder testGroups(List<TestGroup> testGroups) { this.testGroups = testGroups; return this; }
        public DiagnosticReportDTOBuilder signature(DoctorSignature signature) { this.signature = signature; return this; }
        public DiagnosticReportDTOBuilder analytics(AnalyticsSummary analytics) { this.analytics = analytics; return this; }
        public DiagnosticReportDTOBuilder trends(List<LongitudinalTrend> trends) { this.trends = trends; return this; }
        public DiagnosticReportDTOBuilder insights(List<String> insights) { this.insights = insights; return this; }
        public DiagnosticReportDTOBuilder metadata(ReportMetadataDTO metadata) { this.metadata = metadata; return this; }
        public DiagnosticReportDTOBuilder branding(BrandingDTO branding) { this.branding = branding; return this; }
        public DiagnosticReportDTOBuilder withHeader(boolean withHeader) { this.withHeader = withHeader; return this; }

        public DiagnosticReportDTO build() {
            return new DiagnosticReportDTO(reportType, reportTitle, patient, testGroups, signature, analytics, trends, insights, metadata, branding, withHeader);
        }
    }
}
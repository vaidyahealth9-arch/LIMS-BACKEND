package com.halo.lims.dto.organization;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public class OrganizationReportBrandingUpdateRequest {

    private String reportHeaderImage;
    private String reportFooterImage;

    @Pattern(
        regexp = "^$|^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$",
        message = "GSTIN must be a valid 15-character Indian GSTIN"
    )
    private String gstin;

    @Min(value = 0, message = "Header margin must be at least 0 mm")
    @Max(value = 500, message = "Header margin must be at most 500 mm")
    private Integer reportHeaderMarginMm;

    @Min(value = 0, message = "Footer margin must be at least 0 mm")
    @Max(value = 500, message = "Footer margin must be at most 500 mm")
    private Integer reportFooterMarginMm;

    @Min(value = 0, message = "Header height must be at least 0 mm")
    @Max(value = 120, message = "Header height must be at most 120 mm")
    private Integer reportHeaderHeightMm;

    @Min(value = 0, message = "Footer height must be at least 0 mm")
    @Max(value = 120, message = "Footer height must be at most 120 mm")
    private Integer reportFooterHeightMm;

    public OrganizationReportBrandingUpdateRequest() {}

    // Getters and Setters
    public String getReportHeaderImage() { return reportHeaderImage; }
    public void setReportHeaderImage(String reportHeaderImage) { this.reportHeaderImage = reportHeaderImage; }

    public String getReportFooterImage() { return reportFooterImage; }
    public void setReportFooterImage(String reportFooterImage) { this.reportFooterImage = reportFooterImage; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public Integer getReportHeaderMarginMm() { return reportHeaderMarginMm; }
    public void setReportHeaderMarginMm(Integer reportHeaderMarginMm) { this.reportHeaderMarginMm = reportHeaderMarginMm; }

    public Integer getReportFooterMarginMm() { return reportFooterMarginMm; }
    public void setReportFooterMarginMm(Integer reportFooterMarginMm) { this.reportFooterMarginMm = reportFooterMarginMm; }

    public Integer getReportHeaderHeightMm() { return reportHeaderHeightMm; }
    public void setReportHeaderHeightMm(Integer reportHeaderHeightMm) { this.reportHeaderHeightMm = reportHeaderHeightMm; }

    public Integer getReportFooterHeightMm() { return reportFooterHeightMm; }
    public void setReportFooterHeightMm(Integer reportFooterHeightMm) { this.reportFooterHeightMm = reportFooterHeightMm; }
}
package com.halo.lims.dto.report;

public class AnalyteResult {
    private String analyteName;
    private String value;
    private String unit;
    private String referenceRange;
    private String refLowDisplay;
    private String refHighDisplay;
    private String status;
    private String statusClass;
    private boolean abnormal;
    private int markerPercent;
    private int pointCount;
    private String sparklineSvg;
    private String interpretation;

    public AnalyteResult() {}

    public AnalyteResult(String analyteName, String value, String unit, String referenceRange, String refLowDisplay, String refHighDisplay, String status, String statusClass, boolean abnormal, int markerPercent, int pointCount, String sparklineSvg, String interpretation) {
        this.analyteName = analyteName;
        this.value = value;
        this.unit = unit;
        this.referenceRange = referenceRange;
        this.refLowDisplay = refLowDisplay;
        this.refHighDisplay = refHighDisplay;
        this.status = status;
        this.statusClass = statusClass;
        this.abnormal = abnormal;
        this.markerPercent = markerPercent;
        this.pointCount = pointCount;
        this.sparklineSvg = sparklineSvg;
        this.interpretation = interpretation;
    }

    public static AnalyteResultBuilder builder() {
        return new AnalyteResultBuilder();
    }

    // Getters and Setters
    public String getAnalyteName() { return analyteName; }
    public void setAnalyteName(String analyteName) { this.analyteName = analyteName; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getReferenceRange() { return referenceRange; }
    public void setReferenceRange(String referenceRange) { this.referenceRange = referenceRange; }

    public String getRefLowDisplay() { return refLowDisplay; }
    public void setRefLowDisplay(String refLowDisplay) { this.refLowDisplay = refLowDisplay; }

    public String getRefHighDisplay() { return refHighDisplay; }
    public void setRefHighDisplay(String refHighDisplay) { this.refHighDisplay = refHighDisplay; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusClass() { return statusClass; }
    public void setStatusClass(String statusClass) { this.statusClass = statusClass; }

    public boolean isAbnormal() { return abnormal; }
    public void setAbnormal(boolean abnormal) { this.abnormal = abnormal; }

    public int getMarkerPercent() { return markerPercent; }
    public void setMarkerPercent(int markerPercent) { this.markerPercent = markerPercent; }

    public int getPointCount() { return pointCount; }
    public void setPointCount(int pointCount) { this.pointCount = pointCount; }

    public String getSparklineSvg() { return sparklineSvg; }
    public void setSparklineSvg(String sparklineSvg) { this.sparklineSvg = sparklineSvg; }

    public String getInterpretation() { return interpretation; }
    public void setInterpretation(String interpretation) { this.interpretation = interpretation; }

    public static class AnalyteResultBuilder {
        private String analyteName;
        private String value;
        private String unit;
        private String referenceRange;
        private String refLowDisplay;
        private String refHighDisplay;
        private String status;
        private String statusClass;
        private boolean abnormal;
        private int markerPercent;
        private int pointCount;
        private String sparklineSvg;
        private String interpretation;

        public AnalyteResultBuilder analyteName(String analyteName) { this.analyteName = analyteName; return this; }
        public AnalyteResultBuilder value(String value) { this.value = value; return this; }
        public AnalyteResultBuilder unit(String unit) { this.unit = unit; return this; }
        public AnalyteResultBuilder referenceRange(String referenceRange) { this.referenceRange = referenceRange; return this; }
        public AnalyteResultBuilder refLowDisplay(String refLowDisplay) { this.refLowDisplay = refLowDisplay; return this; }
        public AnalyteResultBuilder refHighDisplay(String refHighDisplay) { this.refHighDisplay = refHighDisplay; return this; }
        public AnalyteResultBuilder status(String status) { this.status = status; return this; }
        public AnalyteResultBuilder statusClass(String statusClass) { this.statusClass = statusClass; return this; }
        public AnalyteResultBuilder abnormal(boolean abnormal) { this.abnormal = abnormal; return this; }
        public AnalyteResultBuilder markerPercent(int markerPercent) { this.markerPercent = markerPercent; return this; }
        public AnalyteResultBuilder pointCount(int pointCount) { this.pointCount = pointCount; return this; }
        public AnalyteResultBuilder sparklineSvg(String sparklineSvg) { this.sparklineSvg = sparklineSvg; return this; }
        public AnalyteResultBuilder interpretation(String interpretation) { this.interpretation = interpretation; return this; }

        public AnalyteResult build() {
            return new AnalyteResult(analyteName, value, unit, referenceRange, refLowDisplay, refHighDisplay, status, statusClass, abnormal, markerPercent, pointCount, sparklineSvg, interpretation);
        }
    }
}
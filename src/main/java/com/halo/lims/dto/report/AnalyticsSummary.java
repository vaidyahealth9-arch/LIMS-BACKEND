package com.halo.lims.dto.report;

import java.util.List;

public class AnalyticsSummary {
    private int totalTests;
    private int totalAnalytes;
    private int normalCount;
    private int abnormalCount;
    private int normalPercent;
    private int abnormalPercent;
    private List<PanelVolume> topPanels;

    public AnalyticsSummary() {}

    public AnalyticsSummary(int totalTests, int totalAnalytes, int normalCount, int abnormalCount, int normalPercent, int abnormalPercent, List<PanelVolume> topPanels) {
        this.totalTests = totalTests;
        this.totalAnalytes = totalAnalytes;
        this.normalCount = normalCount;
        this.abnormalCount = abnormalCount;
        this.normalPercent = normalPercent;
        this.abnormalPercent = abnormalPercent;
        this.topPanels = topPanels;
    }

    public static AnalyticsSummaryBuilder builder() {
        return new AnalyticsSummaryBuilder();
    }

    // Getters and Setters
    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public int getTotalAnalytes() { return totalAnalytes; }
    public void setTotalAnalytes(int totalAnalytes) { this.totalAnalytes = totalAnalytes; }

    public int getNormalCount() { return normalCount; }
    public void setNormalCount(int normalCount) { this.normalCount = normalCount; }

    public int getAbnormalCount() { return abnormalCount; }
    public void setAbnormalCount(int abnormalCount) { this.abnormalCount = abnormalCount; }

    public int getNormalPercent() { return normalPercent; }
    public void setNormalPercent(int normalPercent) { this.normalPercent = normalPercent; }

    public int getAbnormalPercent() { return abnormalPercent; }
    public void setAbnormalPercent(int abnormalPercent) { this.abnormalPercent = abnormalPercent; }

    public List<PanelVolume> getTopPanels() { return topPanels; }
    public void setTopPanels(List<PanelVolume> topPanels) { this.topPanels = topPanels; }

    public static class AnalyticsSummaryBuilder {
        private int totalTests;
        private int totalAnalytes;
        private int normalCount;
        private int abnormalCount;
        private int normalPercent;
        private int abnormalPercent;
        private List<PanelVolume> topPanels;

        public AnalyticsSummaryBuilder totalTests(int totalTests) { this.totalTests = totalTests; return this; }
        public AnalyticsSummaryBuilder totalAnalytes(int totalAnalytes) { this.totalAnalytes = totalAnalytes; return this; }
        public AnalyticsSummaryBuilder normalCount(int normalCount) { this.normalCount = normalCount; return this; }
        public AnalyticsSummaryBuilder abnormalCount(int abnormalCount) { this.abnormalCount = abnormalCount; return this; }
        public AnalyticsSummaryBuilder normalPercent(int normalPercent) { this.normalPercent = normalPercent; return this; }
        public AnalyticsSummaryBuilder abnormalPercent(int abnormalPercent) { this.abnormalPercent = abnormalPercent; return this; }
        public AnalyticsSummaryBuilder topPanels(List<PanelVolume> topPanels) { this.topPanels = topPanels; return this; }

        public AnalyticsSummary build() {
            return new AnalyticsSummary(totalTests, totalAnalytes, normalCount, abnormalCount, normalPercent, abnormalPercent, topPanels);
        }
    }
}
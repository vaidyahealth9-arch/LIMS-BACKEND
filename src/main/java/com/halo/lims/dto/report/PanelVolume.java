package com.halo.lims.dto.report;

public class PanelVolume {
    private String panelName;
    private int analyteCount;
    private int abnormalCount;
    private int percentage;

    public PanelVolume() {}

    public PanelVolume(String panelName, int analyteCount, int abnormalCount, int percentage) {
        this.panelName = panelName;
        this.analyteCount = analyteCount;
        this.abnormalCount = abnormalCount;
        this.percentage = percentage;
    }

    public static PanelVolumeBuilder builder() {
        return new PanelVolumeBuilder();
    }

    // Getters and Setters
    public String getPanelName() { return panelName; }
    public void setPanelName(String panelName) { this.panelName = panelName; }

    public int getAnalyteCount() { return analyteCount; }
    public void setAnalyteCount(int analyteCount) { this.analyteCount = analyteCount; }

    public int getAbnormalCount() { return abnormalCount; }
    public void setAbnormalCount(int abnormalCount) { this.abnormalCount = abnormalCount; }

    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }

    public static class PanelVolumeBuilder {
        private String panelName;
        private int analyteCount;
        private int abnormalCount;
        private int percentage;

        public PanelVolumeBuilder panelName(String panelName) { this.panelName = panelName; return this; }
        public PanelVolumeBuilder analyteCount(int analyteCount) { this.analyteCount = analyteCount; return this; }
        public PanelVolumeBuilder abnormalCount(int abnormalCount) { this.abnormalCount = abnormalCount; return this; }
        public PanelVolumeBuilder percentage(int percentage) { this.percentage = percentage; return this; }

        public PanelVolume build() {
            return new PanelVolume(panelName, analyteCount, abnormalCount, percentage);
        }
    }
}
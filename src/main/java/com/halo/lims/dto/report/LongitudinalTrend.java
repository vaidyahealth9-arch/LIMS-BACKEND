package com.halo.lims.dto.report;

import java.util.List;

public class LongitudinalTrend {
    private String label;
    private List<String> values;

    public LongitudinalTrend() {}

    public LongitudinalTrend(String label, List<String> values) {
        this.label = label;
        this.values = values;
    }

    public static LongitudinalTrendBuilder builder() {
        return new LongitudinalTrendBuilder();
    }

    // Getters and Setters
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public List<String> getValues() { return values; }
    public void setValues(List<String> values) { this.values = values; }

    public static class LongitudinalTrendBuilder {
        private String label;
        private List<String> values;

        public LongitudinalTrendBuilder label(String label) { this.label = label; return this; }
        public LongitudinalTrendBuilder values(List<String> values) { this.values = values; return this; }

        public LongitudinalTrend build() {
            return new LongitudinalTrend(label, values);
        }
    }
}
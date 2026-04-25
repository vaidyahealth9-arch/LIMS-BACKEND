package com.halo.lims.dto.report;

import java.util.List;

public class TestGroup {
    private String testName;
    private int analyteCount;
    private boolean hasAbnormalResults;
    private String interpretation;
    private List<AnalyteResult> analytes;

    public TestGroup() {}

    public TestGroup(String testName, int analyteCount, boolean hasAbnormalResults, String interpretation, List<AnalyteResult> analytes) {
        this.testName = testName;
        this.analyteCount = analyteCount;
        this.hasAbnormalResults = hasAbnormalResults;
        this.interpretation = interpretation;
        this.analytes = analytes;
    }

    public static TestGroupBuilder builder() {
        return new TestGroupBuilder();
    }

    // Getters and Setters
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public int getAnalyteCount() { return analyteCount; }
    public void setAnalyteCount(int analyteCount) { this.analyteCount = analyteCount; }

    public boolean isHasAbnormalResults() { return hasAbnormalResults; }
    public void setHasAbnormalResults(boolean hasAbnormalResults) { this.hasAbnormalResults = hasAbnormalResults; }

    public String getInterpretation() { return interpretation; }
    public void setInterpretation(String interpretation) { this.interpretation = interpretation; }

    public List<AnalyteResult> getAnalytes() { return analytes; }
    public void setAnalytes(List<AnalyteResult> analytes) { this.analytes = analytes; }

    // Legacy method support
    public String testName() { return testName; }
    public int analyteCount() { return analyteCount; }
    public boolean hasAbnormalResults() { return hasAbnormalResults; }
    public String interpretation() { return interpretation; }
    public List<AnalyteResult> analytes() { return analytes; }

    public static class TestGroupBuilder {
        private String testName;
        private int analyteCount;
        private boolean hasAbnormalResults;
        private String interpretation;
        private List<AnalyteResult> analytes;

        public TestGroupBuilder testName(String testName) { this.testName = testName; return this; }
        public TestGroupBuilder analyteCount(int analyteCount) { this.analyteCount = analyteCount; return this; }
        public TestGroupBuilder hasAbnormalResults(boolean hasAbnormalResults) { this.hasAbnormalResults = hasAbnormalResults; return this; }
        public TestGroupBuilder interpretation(String interpretation) { this.interpretation = interpretation; return this; }
        public TestGroupBuilder analytes(List<AnalyteResult> analytes) { this.analytes = analytes; return this; }

        public TestGroup build() {
            return new TestGroup(testName, analyteCount, hasAbnormalResults, interpretation, analytes);
        }
    }
}
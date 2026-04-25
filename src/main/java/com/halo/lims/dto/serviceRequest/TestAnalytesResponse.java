package com.halo.lims.dto.serviceRequest;

import java.util.List;

public class TestAnalytesResponse {
    private Integer testId;
    private String testName;
    private List<AnalyteDetailResponse> analytes;

    public TestAnalytesResponse() {}

    public TestAnalytesResponse(Integer testId, String testName, List<AnalyteDetailResponse> analytes) {
        this.testId = testId;
        this.testName = testName;
        this.analytes = analytes;
    }

    public static TestAnalytesResponseBuilder builder() {
        return new TestAnalytesResponseBuilder();
    }

    // Getters and Setters
    public Integer getTestId() { return testId; }
    public void setTestId(Integer testId) { this.testId = testId; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public List<AnalyteDetailResponse> getAnalytes() { return analytes; }
    public void setAnalytes(List<AnalyteDetailResponse> analytes) { this.analytes = analytes; }

    public static class TestAnalytesResponseBuilder {
        private Integer testId;
        private String testName;
        private List<AnalyteDetailResponse> analytes;

        public TestAnalytesResponseBuilder testId(Integer testId) { this.testId = testId; return this; }
        public TestAnalytesResponseBuilder testName(String testName) { this.testName = testName; return this; }
        public TestAnalytesResponseBuilder analytes(List<AnalyteDetailResponse> analytes) { this.analytes = analytes; return this; }

        public TestAnalytesResponse build() {
            return new TestAnalytesResponse(testId, testName, analytes);
        }
    }
}

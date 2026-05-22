package com.halo.lims.dto.serviceRequest;

import com.halo.lims.dto.test.InterpretationRuleResponse;

public class AnalyteDetailResponse {
    private Integer analyteId;
    private String analyteName;
    private String unit;
    private String resultType;
    private Integer decimalPlaces;
    private String referenceRange;
    private String biologicalRefInterval;
    private InterpretationRuleResponse interpretationRule;

    public AnalyteDetailResponse() {}

    public AnalyteDetailResponse(Integer analyteId, String analyteName, String unit, String resultType, Integer decimalPlaces, String referenceRange, String biologicalRefInterval, InterpretationRuleResponse interpretationRule) {
        this.analyteId = analyteId;
        this.analyteName = analyteName;
        this.unit = unit;
        this.resultType = resultType;
        this.decimalPlaces = decimalPlaces;
        this.referenceRange = referenceRange;
        this.biologicalRefInterval = biologicalRefInterval;
        this.interpretationRule = interpretationRule;
    }

    public static AnalyteDetailResponseBuilder builder() {
        return new AnalyteDetailResponseBuilder();
    }

    // Getters and Setters
    public Integer getAnalyteId() { return analyteId; }
    public void setAnalyteId(Integer analyteId) { this.analyteId = analyteId; }

    public String getAnalyteName() { return analyteName; }
    public void setAnalyteName(String analyteName) { this.analyteName = analyteName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getResultType() { return resultType; }
    public void setResultType(String resultType) { this.resultType = resultType; }

    public Integer getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(Integer decimalPlaces) { this.decimalPlaces = decimalPlaces; }

    public String getReferenceRange() { return referenceRange; }
    public void setReferenceRange(String referenceRange) { this.referenceRange = referenceRange; }

    public String getBiologicalRefInterval() { return biologicalRefInterval; }
    public void setBiologicalRefInterval(String biologicalRefInterval) { this.biologicalRefInterval = biologicalRefInterval; }

    public InterpretationRuleResponse getInterpretationRule() { return interpretationRule; }
    public void setInterpretationRule(InterpretationRuleResponse interpretationRule) { this.interpretationRule = interpretationRule; }

    public static class AnalyteDetailResponseBuilder {
        private Integer analyteId;
        private String analyteName;
        private String unit;
        private String resultType;
        private Integer decimalPlaces;
        private String referenceRange;
        private String biologicalRefInterval;
        private InterpretationRuleResponse interpretationRule;

        public AnalyteDetailResponseBuilder analyteId(Integer analyteId) { this.analyteId = analyteId; return this; }
        public AnalyteDetailResponseBuilder analyteName(String analyteName) { this.analyteName = analyteName; return this; }
        public AnalyteDetailResponseBuilder unit(String unit) { this.unit = unit; return this; }
        public AnalyteDetailResponseBuilder resultType(String resultType) { this.resultType = resultType; return this; }
        public AnalyteDetailResponseBuilder decimalPlaces(Integer decimalPlaces) { this.decimalPlaces = decimalPlaces; return this; }
        public AnalyteDetailResponseBuilder referenceRange(String referenceRange) { this.referenceRange = referenceRange; return this; }
        public AnalyteDetailResponseBuilder biologicalRefInterval(String biologicalRefInterval) { this.biologicalRefInterval = biologicalRefInterval; return this; }
        public AnalyteDetailResponseBuilder interpretationRule(InterpretationRuleResponse interpretationRule) { this.interpretationRule = interpretationRule; return this; }

        public AnalyteDetailResponse build() {
            return new AnalyteDetailResponse(analyteId, analyteName, unit, resultType, decimalPlaces, referenceRange, biologicalRefInterval, interpretationRule);
        }
    }
}

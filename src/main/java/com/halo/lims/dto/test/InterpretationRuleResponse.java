package com.halo.lims.dto.test;

import java.time.OffsetDateTime;

public class InterpretationRuleResponse {
    private Integer id;
    private Integer organizationId;
    private Integer analyteId;
    private String analyteName;
    private String conditionExpression;
    private String classification;
    private String autoComment;
    private String reflexActionText;
    private String priority;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String ruleSource; // "Organization" or "Global"

    public InterpretationRuleResponse() {}

    public InterpretationRuleResponse(Integer id, Integer organizationId, Integer analyteId, String analyteName, String conditionExpression, String classification, String autoComment, String reflexActionText, String priority, OffsetDateTime createdAt, OffsetDateTime updatedAt, String ruleSource) {
        this.id = id;
        this.organizationId = organizationId;
        this.analyteId = analyteId;
        this.analyteName = analyteName;
        this.conditionExpression = conditionExpression;
        this.classification = classification;
        this.autoComment = autoComment;
        this.reflexActionText = reflexActionText;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.ruleSource = ruleSource;
    }

    public static InterpretationRuleResponseBuilder builder() {
        return new InterpretationRuleResponseBuilder();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public Integer getAnalyteId() { return analyteId; }
    public void setAnalyteId(Integer analyteId) { this.analyteId = analyteId; }

    public String getAnalyteName() { return analyteName; }
    public void setAnalyteName(String analyteName) { this.analyteName = analyteName; }

    public String getConditionExpression() { return conditionExpression; }
    public void setConditionExpression(String conditionExpression) { this.conditionExpression = conditionExpression; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public String getAutoComment() { return autoComment; }
    public void setAutoComment(String autoComment) { this.autoComment = autoComment; }

    public String getReflexActionText() { return reflexActionText; }
    public void setReflexActionText(String reflexActionText) { this.reflexActionText = reflexActionText; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getRuleSource() { return ruleSource; }
    public void setRuleSource(String ruleSource) { this.ruleSource = ruleSource; }

    public static class InterpretationRuleResponseBuilder {
        private Integer id;
        private Integer organizationId;
        private Integer analyteId;
        private String analyteName;
        private String conditionExpression;
        private String classification;
        private String autoComment;
        private String reflexActionText;
        private String priority;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private String ruleSource;

        public InterpretationRuleResponseBuilder id(Integer id) { this.id = id; return this; }
        public InterpretationRuleResponseBuilder organizationId(Integer organizationId) { this.organizationId = organizationId; return this; }
        public InterpretationRuleResponseBuilder analyteId(Integer analyteId) { this.analyteId = analyteId; return this; }
        public InterpretationRuleResponseBuilder analyteName(String analyteName) { this.analyteName = analyteName; return this; }
        public InterpretationRuleResponseBuilder conditionExpression(String conditionExpression) { this.conditionExpression = conditionExpression; return this; }
        public InterpretationRuleResponseBuilder classification(String classification) { this.classification = classification; return this; }
        public InterpretationRuleResponseBuilder autoComment(String autoComment) { this.autoComment = autoComment; return this; }
        public InterpretationRuleResponseBuilder reflexActionText(String reflexActionText) { this.reflexActionText = reflexActionText; return this; }
        public InterpretationRuleResponseBuilder priority(String priority) { this.priority = priority; return this; }
        public InterpretationRuleResponseBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public InterpretationRuleResponseBuilder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public InterpretationRuleResponseBuilder ruleSource(String ruleSource) { this.ruleSource = ruleSource; return this; }

        public InterpretationRuleResponse build() {
            return new InterpretationRuleResponse(id, organizationId, analyteId, analyteName, conditionExpression, classification, autoComment, reflexActionText, priority, createdAt, updatedAt, ruleSource);
        }
    }
}

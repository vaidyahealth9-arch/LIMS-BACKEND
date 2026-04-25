package com.halo.lims.dto.test;

public class OrganizationAnalyteInterpretationRuleCreateRequest {
    private Integer organizationId;
    private Integer analyteId;
    private String conditionExpression;
    private String classification;
    private String autoComment;
    private String reflexActionText;
    private String priority;

    public OrganizationAnalyteInterpretationRuleCreateRequest() {}

    // Getters and Setters
    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public Integer getAnalyteId() { return analyteId; }
    public void setAnalyteId(Integer analyteId) { this.analyteId = analyteId; }

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
}

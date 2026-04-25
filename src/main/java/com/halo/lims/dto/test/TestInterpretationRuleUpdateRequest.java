package com.halo.lims.dto.test;

import jakarta.validation.constraints.Size;

public class TestInterpretationRuleUpdateRequest {
    private String conditionExpression;

    @Size(max = 100)
    private String classification;

    private String autoComment;

    private String reflexActionText;

    @Size(max = 50)
    private String priority;

    public TestInterpretationRuleUpdateRequest() {}

    // Getters and Setters
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

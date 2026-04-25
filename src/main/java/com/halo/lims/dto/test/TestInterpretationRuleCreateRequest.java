package com.halo.lims.dto.test;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class TestInterpretationRuleCreateRequest {
    @Size(max = 100)
    private String ruleId;

    @NotNull(message = "Analyte ID is required")
    @Min(value = 1, message = "Analyte ID must be positive")
    private Integer analyteId;

    @NotBlank(message = "Condition expression is required")
    private String conditionExpression;

    @Size(max = 100)
    private String classification;

    private String autoComment;

    private String reflexActionText;

    @Size(max = 50)
    private String priority; 

    public TestInterpretationRuleCreateRequest() {}

    // Getters and Setters
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

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

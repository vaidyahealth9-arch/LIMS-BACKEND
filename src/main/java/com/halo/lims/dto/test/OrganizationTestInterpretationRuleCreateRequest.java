package com.halo.lims.dto.test;

import com.halo.lims.model.compositeKeys.OrganizationTestId;

public class OrganizationTestInterpretationRuleCreateRequest {
    private OrganizationTestId organizationTestId;
    private String conditionExpression;
    private String classification;
    private String autoComment;
    private String reflexActionText;
    private String priority;

    public OrganizationTestInterpretationRuleCreateRequest() {}

    // Getters and Setters
    public OrganizationTestId getOrganizationTestId() { return organizationTestId; }
    public void setOrganizationTestId(OrganizationTestId organizationTestId) { this.organizationTestId = organizationTestId; }

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

package com.halo.lims.dto.test;

public class OrganizationAnalyteInterpretationRuleUpdateRequest {
    private String classification;
    private String autoComment;
    private String reflexActionText;
    private String priority;

    public OrganizationAnalyteInterpretationRuleUpdateRequest() {}

    // Getters and Setters
    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public String getAutoComment() { return autoComment; }
    public void setAutoComment(String autoComment) { this.autoComment = autoComment; }

    public String getReflexActionText() { return reflexActionText; }
    public void setReflexActionText(String reflexActionText) { this.reflexActionText = reflexActionText; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}

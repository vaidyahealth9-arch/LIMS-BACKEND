package com.halo.lims.dto.test;

import lombok.Data;

@Data
public class OrganizationAnalyteInterpretationRuleCreateRequest {
    private Integer organizationId;
    private Integer analyteId;
    private String conditionExpression;
    private String classification;
    private String autoComment;
    private String reflexActionText;
    private String priority;
}

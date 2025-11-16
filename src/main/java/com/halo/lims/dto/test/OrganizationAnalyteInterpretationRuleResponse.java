package com.halo.lims.dto.test;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class OrganizationAnalyteInterpretationRuleResponse {
    private Integer id;
    private Integer organizationId;
    private Integer analyteId;
    private String conditionExpression;
    private String classification;
    private String autoComment;
    private String reflexActionText;
    private String priority;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

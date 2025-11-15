package com.halo.lims.dto.test;

import com.halo.lims.model.compositeKeys.OrganizationTestId;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class OrganizationTestInterpretationRuleResponse {
    private Integer id;
    private OrganizationTestId organizationTestId;
    private String conditionExpression;
    private String classification;
    private String autoComment;
    private String reflexActionText;
    private String priority;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

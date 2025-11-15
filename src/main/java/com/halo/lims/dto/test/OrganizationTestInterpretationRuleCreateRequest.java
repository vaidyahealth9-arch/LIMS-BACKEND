package com.halo.lims.dto.test;

import com.halo.lims.model.compositeKeys.OrganizationTestId;
import lombok.Data;

@Data
public class OrganizationTestInterpretationRuleCreateRequest {
    private OrganizationTestId organizationTestId;
    private String conditionExpression;
    private String classification;
    private String autoComment;
    private String reflexActionText;
    private String priority;
}

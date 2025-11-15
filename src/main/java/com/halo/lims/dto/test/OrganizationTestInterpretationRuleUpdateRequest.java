package com.halo.lims.dto.test;

import lombok.Data;

@Data
public class OrganizationTestInterpretationRuleUpdateRequest {
    private String classification;
    private String autoComment;
    private String reflexActionText;
    private String priority;
}

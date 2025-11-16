package com.halo.lims.dto.test;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterpretationRuleResponse {
    private Integer id;
    private Integer organizationId;
    private Integer analyteId;
    private String analyteName;
    private String conditionExpression;
    private String classification;
    private String autoComment;
    private String reflexActionText;
    private String priority;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String ruleSource; // "Organization" or "Global"
}

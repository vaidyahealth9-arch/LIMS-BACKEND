package com.halo.lims.dto.test;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class TestInterpretationRuleResponse {
    private Integer id;
    private String ruleId;
    private Integer analyteId;
    private String analyteCode;
    private String analyteName;
    private String conditionExpression;
    private String classification;
    private String autoComment;
    private String reflexActionText;
    private String priority;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

package com.halo.lims.dto.test;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TestInterpretationRuleUpdateRequest {
    private String conditionExpression;

    @Size(max = 100)
    private String classification;

    private String autoComment;

    private String reflexActionText;

    @Size(max = 50)
    private String priority;
}

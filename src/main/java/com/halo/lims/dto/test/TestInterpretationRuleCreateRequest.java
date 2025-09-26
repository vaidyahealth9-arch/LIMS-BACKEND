package com.halo.lims.dto.test;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TestInterpretationRuleCreateRequest {
    @NotBlank(message = "Rule ID is required")
    @Size(max = 100)
    private String ruleId;

    @NotNull(message = "Analyte ID is required")
    @Min(value = 1, message = "Analyte ID must be positive")
    private Integer analyteId;

    @NotBlank(message = "Condition expression is required")
    private String conditionExpression;

    @Size(max = 100)
    private String classification;

    private String autoComment;

    private String reflexActionText;

    @Size(max = 50)
    private String priority; // "Info", "Routine", "Priority", "STAT", "Critical"
}

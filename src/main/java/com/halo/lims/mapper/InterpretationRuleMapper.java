package com.halo.lims.mapper;

import com.halo.lims.dto.test.InterpretationRuleResponse;
import com.halo.lims.model.OrganizationAnalyteInterpretationRule;
import com.halo.lims.model.TestAnalyte;
import com.halo.lims.model.TestInterpretationRule;
import org.springframework.stereotype.Component;

@Component
public class InterpretationRuleMapper {

    public InterpretationRuleResponse mapToInterpretationRuleResponse(OrganizationAnalyteInterpretationRule orgRule) {
        return InterpretationRuleResponse.builder()
                .id(orgRule.getId())
                .organizationId(orgRule.getOrganization().getId())
                .analyteId(orgRule.getAnalyte().getId())
                .analyteName(orgRule.getAnalyte().getAnalyteName())
                .conditionExpression(orgRule.getConditionExpression())
                .classification(orgRule.getClassification())
                .autoComment(orgRule.getAutoComment())
                .reflexActionText(orgRule.getReflexActionText())
                .priority(orgRule.getPriority())
                .createdAt(orgRule.getCreatedAt())
                .updatedAt(orgRule.getUpdatedAt())
                .ruleSource("Organization")
                .build();
    }

    public InterpretationRuleResponse mapToInterpretationRuleResponse(TestInterpretationRule globalRule) {
        return InterpretationRuleResponse.builder()
                .id(globalRule.getId())
                .analyteId(globalRule.getAnalyte().getId())
                .analyteName(globalRule.getAnalyte().getAnalyteName())
                .conditionExpression(globalRule.getConditionExpression())
                .classification(globalRule.getClassification())
                .autoComment(globalRule.getAutoComment())
                .reflexActionText(globalRule.getReflexActionText())
                .priority(globalRule.getPriority())
                .createdAt(globalRule.getCreatedAt())
                .updatedAt(globalRule.getUpdatedAt())
                .ruleSource("Global")
                .build();
    }

    public InterpretationRuleResponse mapToInterpretationRuleResponse(TestAnalyte analyte) {
        return InterpretationRuleResponse.builder()
                .analyteId(analyte.getId())
                .analyteName(analyte.getAnalyteName())
                .build();
    }
}

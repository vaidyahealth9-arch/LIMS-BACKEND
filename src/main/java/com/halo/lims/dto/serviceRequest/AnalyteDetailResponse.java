package com.halo.lims.dto.serviceRequest;

import com.halo.lims.dto.test.InterpretationRuleResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyteDetailResponse {
    private Integer analyteId;
    private String analyteName;
    private String unit;
    private InterpretationRuleResponse interpretationRule;
}

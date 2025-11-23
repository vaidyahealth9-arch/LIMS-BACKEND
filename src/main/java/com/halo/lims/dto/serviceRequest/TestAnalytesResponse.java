package com.halo.lims.dto.serviceRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAnalytesResponse {
    private Integer testId;
    private String testName;
    private List<AnalyteDetailResponse> analytes;
}

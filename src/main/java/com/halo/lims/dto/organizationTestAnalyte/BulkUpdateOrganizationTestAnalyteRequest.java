package com.halo.lims.dto.organizationTestAnalyte;

import java.util.List;

public class BulkUpdateOrganizationTestAnalyteRequest {
    private List<Integer> analyteIds;

    public BulkUpdateOrganizationTestAnalyteRequest() {}

    // Getters and Setters
    public List<Integer> getAnalyteIds() { return analyteIds; }
    public void setAnalyteIds(List<Integer> analyteIds) { this.analyteIds = analyteIds; }
}

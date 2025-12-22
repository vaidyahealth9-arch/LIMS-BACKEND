package com.halo.lims.dto.organizationTestAnalyte;

import lombok.Data;

import java.util.List;

@Data
public class BulkUpdateOrganizationTestAnalyteRequest {
    private List<Integer> analyteIds;
}

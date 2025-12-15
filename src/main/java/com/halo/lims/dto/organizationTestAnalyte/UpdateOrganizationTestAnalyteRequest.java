package com.halo.lims.dto.organizationTestAnalyte;

import lombok.Data;

@Data
public class UpdateOrganizationTestAnalyteRequest {
    private String resultType;
    private Integer decimalPlaces;
    private String biologicalRefInterval;
}

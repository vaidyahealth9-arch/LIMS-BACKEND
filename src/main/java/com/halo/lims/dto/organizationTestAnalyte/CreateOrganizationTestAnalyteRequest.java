package com.halo.lims.dto.organizationTestAnalyte;

import lombok.Data;

@Data
public class CreateOrganizationTestAnalyteRequest {
    private Integer organizationId;
    private Integer testAnalyteId;
    private String resultType;
    private Integer decimalPlaces;
    private String biologicalRefInterval;
}

package com.halo.lims.dto.organization.test;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class OrganizationTestResponse {
    private Integer organizationId;
    private String organizationName;
    private Integer testId;
    private String testLocalCode;
    private String testName;
    private Boolean isEnabled;
    private BigDecimal price;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

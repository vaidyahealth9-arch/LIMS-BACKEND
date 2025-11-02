package com.halo.lims.dto.unit;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class UnitResponse {
    private Integer id;
    private String name;
    private String ucumCode;
    private String description;
    private Integer organizationId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

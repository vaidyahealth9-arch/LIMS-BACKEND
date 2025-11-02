package com.halo.lims.dto.unit;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UnitUpdateRequest {
    private Integer organizationId;

    @Size(max = 50)
    private String name; // Can allow updating name, but ucumCode should ideally be fixed

    private String description;
}

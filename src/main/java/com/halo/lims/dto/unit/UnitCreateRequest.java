package com.halo.lims.dto.unit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UnitCreateRequest {
    private Integer organizationId;

    @NotBlank(message = "Unit name is required")
    @Size(max = 50)
    private String name;

    @NotBlank(message = "UCUM Code is required")
    @Size(max = 50)
    private String ucumCode;

    private String description; // TEXT field, no @Size max
}

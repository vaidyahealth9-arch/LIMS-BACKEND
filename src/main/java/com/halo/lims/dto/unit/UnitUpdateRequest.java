package com.halo.lims.dto.unit;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UnitUpdateRequest {
    @Size(max = 50)
    private String name; // Can allow updating name, but ucumCode should ideally be fixed

    private String description;
}

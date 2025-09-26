package com.halo.lims.dto.specimenType;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SpecimenTypeUpdateRequest {
    @Size(max = 100)
    private String name;

    @Size(max = 50)
    private String snomedCode;

    @Size(max = 255)
    private String snomedSystem;

    private String description;
}

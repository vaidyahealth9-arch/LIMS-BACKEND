package com.halo.lims.dto.specimenType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SpecimenTypeCreateRequest {
    @NotBlank(message = "Specimen type name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 50)
    private String snomedCode;

    @Size(max = 255)
    private String snomedSystem; // Default to 'http://snomed.info/sct'

    private String description; // TEXT field
}

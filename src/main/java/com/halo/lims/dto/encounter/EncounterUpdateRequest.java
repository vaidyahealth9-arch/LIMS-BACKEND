package com.halo.lims.dto.encounter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class EncounterUpdateRequest {
    private OffsetDateTime endTime;

    @NotBlank(message = "Status is required")
    @Size(max = 50)
    private String status;

    @NotBlank(message = "Encounter class is required")
    @Size(max = 100)
    private String encounterClass;
}

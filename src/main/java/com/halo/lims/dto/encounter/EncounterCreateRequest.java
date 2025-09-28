package com.halo.lims.dto.encounter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class EncounterCreateRequest {
    @NotNull(message = "Patient ID is required")
    @Min(value = 1, message = "Patient ID must be positive")
    private Integer patientId;

    @NotNull(message = "Start time is required")
    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    @NotBlank(message = "Status is required")
    @Size(max = 50)
    private String status; // FHIR EncounterStatus

    @NotBlank(message = "Encounter class is required")
    @Size(max = 100)
    private String encounterClass; // FHIR EncounterClass

    @NotNull(message = "Service provider ID is required")
    @Min(value = 1, message = "Service provider ID must be positive")
    private Integer serviceProviderId; // The organization that provided the encounter

    @Size(max = 100)
    private String referenceDoctor;
}

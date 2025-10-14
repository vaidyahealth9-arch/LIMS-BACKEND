package com.halo.lims.dto.encounter;

import com.halo.lims.constant.EncounterClass;
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

    @Size(max = 100)
    private String encounterClass = EncounterClass.AMBULATORY.getCode();
}

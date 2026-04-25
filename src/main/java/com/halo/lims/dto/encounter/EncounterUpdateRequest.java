package com.halo.lims.dto.encounter;

import com.halo.lims.constant.EncounterClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncounterUpdateRequest {
    private OffsetDateTime endTime;

    @NotBlank(message = "Status is required")
    @Size(max = 50)
    private String status;

    @Size(max = 100)
    private String encounterClass = EncounterClass.AMBULATORY.getCode();

    // Getters and Setters
    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEncounterClass() { return encounterClass; }
    public void setEncounterClass(String encounterClass) { this.encounterClass = encounterClass; }
}

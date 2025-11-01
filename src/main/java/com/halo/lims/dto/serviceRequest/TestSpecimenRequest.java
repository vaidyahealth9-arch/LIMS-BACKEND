package com.halo.lims.dto.serviceRequest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TestSpecimenRequest {
    @NotNull
    @Min(1)
    private Integer testId;

    @NotNull
    @Min(1)
    private Integer specimenTypeId;

    @NotNull
    @Min(1)
    private Integer numberOfSpecimens;
}

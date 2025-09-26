package com.halo.lims.dto.specimen;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class SpecimenUpdateRequest {
    private Integer specimenTypeId;
    private OffsetDateTime collectionDate;
    private OffsetDateTime receivedDate;

    @NotBlank(message = "Status is required")
    @Size(max = 50)
    private String status;

    @Size(max = 255)
    private String containerId;

    @DecimalMin(value = "0.0", message = "Quantity value must be non-negative")
    private BigDecimal quantityValue;

    private Integer quantityUnitId;
}

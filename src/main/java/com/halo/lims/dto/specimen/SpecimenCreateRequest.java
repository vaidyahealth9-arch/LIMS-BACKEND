package com.halo.lims.dto.specimen;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class SpecimenCreateRequest {
    @NotNull(message = "Service Request ID is required")
    @Min(value = 1, message = "Service Request ID must be positive")
    private Integer serviceRequestId;

    @NotNull(message = "Specimen Type ID is required")
    @Min(value = 1, message = "Specimen Type ID must be positive")
    private Integer specimenTypeId;

    @NotNull(message = "Collection date is required")
    private OffsetDateTime collectionDate;

    private OffsetDateTime receivedDate;

    @NotBlank(message = "Status is required")
    @Size(max = 50)
    private String status; // FHIR SpecimenStatus

    @NotBlank(message = "Container ID is required")
    @Size(max = 255)
    private String containerId;

    @DecimalMin(value = "0.0", message = "Quantity value must be non-negative")
    private BigDecimal quantityValue;

    private Integer quantityUnitId;
}

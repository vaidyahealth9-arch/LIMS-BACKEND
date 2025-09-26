package com.halo.lims.dto.serviceRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class ServiceRequestCreateRequest {
    @NotNull(message = "Patient ID is required")
    @Min(value = 1, message = "Patient ID must be positive")
    private Integer patientId;

    @NotNull(message = "Requester ID is required")
    @Min(value = 1, message = "Requester ID must be positive")
    private Integer requesterId;

    private Integer encounterId;

    @NotBlank(message = "Status is required")
    @Size(max = 50)
    private String status; // FHIR ServiceRequestStatus

    @Size(max = 20)
    private String priority; // FHIR RequestPriority

    @NotNull(message = "At least one Test ID is required")
    private List<Integer> testIds; // List of global Test IDs being requested
}
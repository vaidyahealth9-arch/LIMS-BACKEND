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

    private Integer patientId;

    @NotNull(message = "Requester ID is required")
    @Min(value = 1, message = "Requester ID must be positive")
    private Integer requesterId;

    @NotNull(message = "Encounter ID is required")
    @Min(value = 1, message = "Encounter ID must be positive")
    private Integer encounterId;

    @NotBlank(message = "Status is required")
    @Size(max = 50)
    private String status; // FHIR ServiceRequestStatus

    @Size(max = 20)
    private String priority; // FHIR RequestPriority

    @NotNull(message = "At least one Test is required")
    private List<TestSpecimenRequest> tests; // List of tests and specimen requirements
}
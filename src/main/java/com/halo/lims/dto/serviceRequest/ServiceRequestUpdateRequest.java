package com.halo.lims.dto.serviceRequest;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ServiceRequestUpdateRequest {
    private Integer requesterId;
    private Integer encounterId;

    @NotBlank(message = "Status is required")
    @Size(max = 50)
    private String status;

    @Size(max = 20)
    private String priority;

    private List<Integer> testIds; // Allows adding/removing tests from the request (careful with existing observations!)
}

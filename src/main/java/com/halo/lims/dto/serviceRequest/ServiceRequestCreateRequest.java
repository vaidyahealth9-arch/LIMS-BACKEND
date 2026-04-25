package com.halo.lims.dto.serviceRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;

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
    private String status; 

    @Size(max = 20)
    private String priority; 

    @NotNull(message = "At least one Test is required")
    private List<TestSpecimenRequest> tests; 

    public ServiceRequestCreateRequest() {}

    // Getters and Setters
    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public Integer getRequesterId() { return requesterId; }
    public void setRequesterId(Integer requesterId) { this.requesterId = requesterId; }

    public Integer getEncounterId() { return encounterId; }
    public void setEncounterId(Integer encounterId) { this.encounterId = encounterId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public List<TestSpecimenRequest> getTests() { return tests; }
    public void setTests(List<TestSpecimenRequest> tests) { this.tests = tests; }
}
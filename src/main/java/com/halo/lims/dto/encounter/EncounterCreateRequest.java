package com.halo.lims.dto.encounter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class EncounterCreateRequest {
    @NotNull(message = "Patient ID is required")
    @Min(value = 1, message = "Patient ID must be positive")
    private Integer patientId;

    @NotNull(message = "Start time is required")
    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    @NotBlank(message = "Status is required")
    @Size(max = 50)
    private String status; 

    @NotBlank(message = "Encounter class is required")
    @Size(max = 100)
    private String encounterClass; 

    @NotNull(message = "Service provider ID is required")
    @Min(value = 1, message = "Service provider ID must be positive")
    private Integer serviceProviderId; 

    @Size(max = 100)
    private String referenceDoctor;

    public EncounterCreateRequest() {}

    public EncounterCreateRequest(Integer patientId, OffsetDateTime startTime, OffsetDateTime endTime, String status, String encounterClass, Integer serviceProviderId, String referenceDoctor) {
        this.patientId = patientId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.encounterClass = encounterClass;
        this.serviceProviderId = serviceProviderId;
        this.referenceDoctor = referenceDoctor;
    }

    public static EncounterCreateRequestBuilder builder() {
        return new EncounterCreateRequestBuilder();
    }

    // Getters and Setters
    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }

    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEncounterClass() { return encounterClass; }
    public void setEncounterClass(String encounterClass) { this.encounterClass = encounterClass; }

    public Integer getServiceProviderId() { return serviceProviderId; }
    public void setServiceProviderId(Integer serviceProviderId) { this.serviceProviderId = serviceProviderId; }

    public String getReferenceDoctor() { return referenceDoctor; }
    public void setReferenceDoctor(String referenceDoctor) { this.referenceDoctor = referenceDoctor; }

    public static class EncounterCreateRequestBuilder {
        private Integer patientId;
        private OffsetDateTime startTime;
        private OffsetDateTime endTime;
        private String status;
        private String encounterClass;
        private Integer serviceProviderId;
        private String referenceDoctor;

        public EncounterCreateRequestBuilder patientId(Integer patientId) { this.patientId = patientId; return this; }
        public EncounterCreateRequestBuilder startTime(OffsetDateTime startTime) { this.startTime = startTime; return this; }
        public EncounterCreateRequestBuilder endTime(OffsetDateTime endTime) { this.endTime = endTime; return this; }
        public EncounterCreateRequestBuilder status(String status) { this.status = status; return this; }
        public EncounterCreateRequestBuilder encounterClass(String encounterClass) { this.encounterClass = encounterClass; return this; }
        public EncounterCreateRequestBuilder serviceProviderId(Integer serviceProviderId) { this.serviceProviderId = serviceProviderId; return this; }
        public EncounterCreateRequestBuilder referenceDoctor(String referenceDoctor) { this.referenceDoctor = referenceDoctor; return this; }

        public EncounterCreateRequest build() {
            return new EncounterCreateRequest(patientId, startTime, endTime, status, encounterClass, serviceProviderId, referenceDoctor);
        }
    }
}

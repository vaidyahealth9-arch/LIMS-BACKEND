package com.halo.lims.dto.encounter;

import java.time.OffsetDateTime;

public class EncounterResponse {
    private Integer id;
    private String localEncounterValue;
    private Integer patientId;
    private String patientMrn;
    private String patientName;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String status;
    private String encounterClass;
    private Integer serviceProviderId;
    private String serviceProviderName;
    private Integer organizationId; // Organization of the patient
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String referenceDoctor;
    private String localEncounterSystem; // Added to match setters in EncounterService

    public EncounterResponse() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLocalEncounterValue() { return localEncounterValue; }
    public void setLocalEncounterValue(String localEncounterValue) { this.localEncounterValue = localEncounterValue; }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public String getPatientMrn() { return patientMrn; }
    public void setPatientMrn(String patientMrn) { this.patientMrn = patientMrn; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

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

    public String getServiceProviderName() { return serviceProviderName; }
    public void setServiceProviderName(String serviceProviderName) { this.serviceProviderName = serviceProviderName; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getReferenceDoctor() { return referenceDoctor; }
    public void setReferenceDoctor(String referenceDoctor) { this.referenceDoctor = referenceDoctor; }

    public String getLocalEncounterSystem() { return localEncounterSystem; }
    public void setLocalEncounterSystem(String localEncounterSystem) { this.localEncounterSystem = localEncounterSystem; }
}

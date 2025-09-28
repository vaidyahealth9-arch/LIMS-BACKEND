package com.halo.lims.dto.encounter;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
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
}

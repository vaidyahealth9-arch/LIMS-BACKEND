package com.halo.lims.dto.encounter;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class EncounterDetailResponse {
    private Integer id;
    private Integer patientId;
    private String patientName;
    private String patientAge;
    private String patientGender;
    private String mrnId;
    private String referenceDoctor;
    private OffsetDateTime date;
    private OffsetDateTime collectionDate;
    private String sampleType;
    private String status;
    private String localEncounterValue;
    private List<String> tests;
    private List<Integer> serviceRequestIds;
}

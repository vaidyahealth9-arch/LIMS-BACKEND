package com.halo.lims.dto.encounter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncounterListResponse {
    private Integer id;
    private String patientName;
    private String mrnId;
    private String referenceDoctor;
    private OffsetDateTime date;
    private String status;
    private List<String> tests;
}

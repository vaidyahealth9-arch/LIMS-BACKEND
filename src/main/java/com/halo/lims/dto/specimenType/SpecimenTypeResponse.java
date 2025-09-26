package com.halo.lims.dto.specimenType;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class SpecimenTypeResponse {
    private Integer id;
    private String name;
    private String snomedCode;
    private String snomedSystem;
    private String description;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
package com.halo.lims.dto.specimen;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class SpecimenResponse {
    private Integer id;
    private String localSpecimenValue;
    private Integer serviceRequestId;
    private String serviceRequestLocalValue;
    private Integer patientId;
    private String patientMrn;
    private Integer specimenTypeId;
    private String specimenTypeName;
    private OffsetDateTime collectionDate;
    private OffsetDateTime receivedDate;
    private String status;
    private String containerId;
    private String barcode;
    private BigDecimal quantityValue;
    private Integer quantityUnitId;
    private String quantityUnitName;
    private Integer organizationId; // Organization of the patient
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
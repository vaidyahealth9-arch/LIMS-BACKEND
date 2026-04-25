package com.halo.lims.dto.specimen;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

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
    private Integer organizationId; 
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public SpecimenResponse() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLocalSpecimenValue() { return localSpecimenValue; }
    public void setLocalSpecimenValue(String localSpecimenValue) { this.localSpecimenValue = localSpecimenValue; }

    public Integer getServiceRequestId() { return serviceRequestId; }
    public void setServiceRequestId(Integer serviceRequestId) { this.serviceRequestId = serviceRequestId; }

    public String getServiceRequestLocalValue() { return serviceRequestLocalValue; }
    public void setServiceRequestLocalValue(String serviceRequestLocalValue) { this.serviceRequestLocalValue = serviceRequestLocalValue; }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public String getPatientMrn() { return patientMrn; }
    public void setPatientMrn(String patientMrn) { this.patientMrn = patientMrn; }

    public Integer getSpecimenTypeId() { return specimenTypeId; }
    public void setSpecimenTypeId(Integer specimenTypeId) { this.specimenTypeId = specimenTypeId; }

    public String getSpecimenTypeName() { return specimenTypeName; }
    public void setSpecimenTypeName(String specimenTypeName) { this.specimenTypeName = specimenTypeName; }

    public OffsetDateTime getCollectionDate() { return collectionDate; }
    public void setCollectionDate(OffsetDateTime collectionDate) { this.collectionDate = collectionDate; }

    public OffsetDateTime getReceivedDate() { return receivedDate; }
    public void setReceivedDate(OffsetDateTime receivedDate) { this.receivedDate = receivedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getContainerId() { return containerId; }
    public void setContainerId(String containerId) { this.containerId = containerId; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public BigDecimal getQuantityValue() { return quantityValue; }
    public void setQuantityValue(BigDecimal quantityValue) { this.quantityValue = quantityValue; }

    public Integer getQuantityUnitId() { return quantityUnitId; }
    public void setQuantityUnitId(Integer quantityUnitId) { this.quantityUnitId = quantityUnitId; }

    public String getQuantityUnitName() { return quantityUnitName; }
    public void setQuantityUnitName(String quantityUnitName) { this.quantityUnitName = quantityUnitName; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
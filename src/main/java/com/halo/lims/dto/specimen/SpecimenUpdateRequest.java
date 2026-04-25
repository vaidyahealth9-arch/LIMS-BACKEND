package com.halo.lims.dto.specimen;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class SpecimenUpdateRequest {
    private Integer specimenTypeId;
    private OffsetDateTime collectionDate;
    private OffsetDateTime receivedDate;

    @NotBlank(message = "Status is required")
    @Size(max = 50)
    private String status;

    @Size(max = 255)
    private String containerId;

    @DecimalMin(value = "0.0", message = "Quantity value must be non-negative")
    private BigDecimal quantityValue;

    private Integer quantityUnitId;

    public SpecimenUpdateRequest() {}

    // Getters and Setters
    public Integer getSpecimenTypeId() { return specimenTypeId; }
    public void setSpecimenTypeId(Integer specimenTypeId) { this.specimenTypeId = specimenTypeId; }

    public OffsetDateTime getCollectionDate() { return collectionDate; }
    public void setCollectionDate(OffsetDateTime collectionDate) { this.collectionDate = collectionDate; }

    public OffsetDateTime getReceivedDate() { return receivedDate; }
    public void setReceivedDate(OffsetDateTime receivedDate) { this.receivedDate = receivedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getContainerId() { return containerId; }
    public void setContainerId(String containerId) { this.containerId = containerId; }

    public BigDecimal getQuantityValue() { return quantityValue; }
    public void setQuantityValue(BigDecimal quantityValue) { this.quantityValue = quantityValue; }

    public Integer getQuantityUnitId() { return quantityUnitId; }
    public void setQuantityUnitId(Integer quantityUnitId) { this.quantityUnitId = quantityUnitId; }
}

package com.halo.lims.dto.billing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class BillCreateRequest {
    @NotNull(message = "Encounter ID is required")
    @Min(value = 1, message = "Encounter ID must be positive")
    private Integer encounterId;

    @NotNull(message = "At least one Service Request ID is required for the bill")
    private List<Integer> serviceRequestIds; 

    @DecimalMin(value = "0.0", message = "Discount percentage must be non-negative")
    private BigDecimal discountPercentage = BigDecimal.ZERO; 

    @Size(max = 50)
    private String initialPaymentMethod; 

    @DecimalMin(value = "0.0", message = "Initial paid amount must be non-negative")
    private BigDecimal initialPaidAmount = BigDecimal.ZERO;

    private String notes;
    private OffsetDateTime dueDate;

    public BillCreateRequest() {}

    // Getters and Setters
    public Integer getEncounterId() { return encounterId; }
    public void setEncounterId(Integer encounterId) { this.encounterId = encounterId; }

    public List<Integer> getServiceRequestIds() { return serviceRequestIds; }
    public void setServiceRequestIds(List<Integer> serviceRequestIds) { this.serviceRequestIds = serviceRequestIds; }

    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }

    public String getInitialPaymentMethod() { return initialPaymentMethod; }
    public void setInitialPaymentMethod(String initialPaymentMethod) { this.initialPaymentMethod = initialPaymentMethod; }

    public BigDecimal getInitialPaidAmount() { return initialPaidAmount; }
    public void setInitialPaidAmount(BigDecimal initialPaidAmount) { this.initialPaidAmount = initialPaidAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public OffsetDateTime getDueDate() { return dueDate; }
    public void setDueDate(OffsetDateTime dueDate) { this.dueDate = dueDate; }
}

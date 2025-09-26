package com.halo.lims.dto.billing;

import com.halo.lims.dto.serviceRequest.ServiceRequestResponse;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class BillResponse {
    private Integer id;
    private String invoiceNumber;
    private OffsetDateTime invoiceDate;
    private OffsetDateTime dueDate;

    private Integer encounterId;
    private String encounterLocalValue;

    private Integer patientId;
    private String patientMrn;
    private String patientName;
    private String patientContactPhone; // Will be decrypted in service

    private Integer organizationId;
    private String organizationName;

    private BigDecimal totalAmount;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal netAmount;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;

    private String status;
    private String paymentMethod;
    private OffsetDateTime paymentDate;
    private String notes;

    private List<BillServiceRequestDetails> serviceRequests; // Details of service requests covered by this bill

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Data
    public static class BillServiceRequestDetails {
        private Integer serviceRequestId;
        private String serviceRequestLocalValue;
        private String status; // Status of SR itself
        private String priority;
        private List<ServiceRequestResponse.TestDetailsResponse> requestedTests;
    }
}

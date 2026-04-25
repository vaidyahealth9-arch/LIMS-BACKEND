package com.halo.lims.dto.billing;

import com.halo.lims.dto.serviceRequest.ServiceRequestResponse;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

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
    private String patientContactPhone; 

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

    private List<BillServiceRequestDetails> serviceRequests; 

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public BillResponse() {}

    public BillResponse(Integer id, String invoiceNumber, OffsetDateTime invoiceDate, OffsetDateTime dueDate, Integer encounterId, String encounterLocalValue, Integer patientId, String patientMrn, String patientName, String patientContactPhone, Integer organizationId, String organizationName, BigDecimal totalAmount, BigDecimal discountPercentage, BigDecimal discountAmount, BigDecimal netAmount, BigDecimal paidAmount, BigDecimal dueAmount, String status, String paymentMethod, OffsetDateTime paymentDate, String notes, List<BillServiceRequestDetails> serviceRequests, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.encounterId = encounterId;
        this.encounterLocalValue = encounterLocalValue;
        this.patientId = patientId;
        this.patientMrn = patientMrn;
        this.patientName = patientName;
        this.patientContactPhone = patientContactPhone;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.totalAmount = totalAmount;
        this.discountPercentage = discountPercentage;
        this.discountAmount = discountAmount;
        this.netAmount = netAmount;
        this.paidAmount = paidAmount;
        this.dueAmount = dueAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.notes = notes;
        this.serviceRequests = serviceRequests;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BillResponseBuilder builder() {
        return new BillResponseBuilder();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public OffsetDateTime getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(OffsetDateTime invoiceDate) { this.invoiceDate = invoiceDate; }

    public OffsetDateTime getDueDate() { return dueDate; }
    public void setDueDate(OffsetDateTime dueDate) { this.dueDate = dueDate; }

    public Integer getEncounterId() { return encounterId; }
    public void setEncounterId(Integer encounterId) { this.encounterId = encounterId; }

    public String getEncounterLocalValue() { return encounterLocalValue; }
    public void setEncounterLocalValue(String encounterLocalValue) { this.encounterLocalValue = encounterLocalValue; }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public String getPatientMrn() { return patientMrn; }
    public void setPatientMrn(String patientMrn) { this.patientMrn = patientMrn; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientContactPhone() { return patientContactPhone; }
    public void setPatientContactPhone(String patientContactPhone) { this.patientContactPhone = patientContactPhone; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public BigDecimal getDueAmount() { return dueAmount; }
    public void setDueAmount(BigDecimal dueAmount) { this.dueAmount = dueAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public OffsetDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(OffsetDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<BillServiceRequestDetails> getServiceRequests() { return serviceRequests; }
    public void setServiceRequests(List<BillServiceRequestDetails> serviceRequests) { this.serviceRequests = serviceRequests; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class BillResponseBuilder {
        private Integer id;
        private String invoiceNumber;
        private OffsetDateTime invoiceDate;
        private OffsetDateTime dueDate;
        private Integer encounterId;
        private String encounterLocalValue;
        private Integer patientId;
        private String patientMrn;
        private String patientName;
        private String patientContactPhone;
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
        private List<BillServiceRequestDetails> serviceRequests;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public BillResponseBuilder id(Integer id) { this.id = id; return this; }
        public BillResponseBuilder invoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; return this; }
        public BillResponseBuilder invoiceDate(OffsetDateTime invoiceDate) { this.invoiceDate = invoiceDate; return this; }
        public BillResponseBuilder dueDate(OffsetDateTime dueDate) { this.dueDate = dueDate; return this; }
        public BillResponseBuilder encounterId(Integer encounterId) { this.encounterId = encounterId; return this; }
        public BillResponseBuilder encounterLocalValue(String encounterLocalValue) { this.encounterLocalValue = encounterLocalValue; return this; }
        public BillResponseBuilder patientId(Integer patientId) { this.patientId = patientId; return this; }
        public BillResponseBuilder patientMrn(String patientMrn) { this.patientMrn = patientMrn; return this; }
        public BillResponseBuilder patientName(String patientName) { this.patientName = patientName; return this; }
        public BillResponseBuilder patientContactPhone(String patientContactPhone) { this.patientContactPhone = patientContactPhone; return this; }
        public BillResponseBuilder organizationId(Integer organizationId) { this.organizationId = organizationId; return this; }
        public BillResponseBuilder organizationName(String organizationName) { this.organizationName = organizationName; return this; }
        public BillResponseBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public BillResponseBuilder discountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; return this; }
        public BillResponseBuilder discountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; return this; }
        public BillResponseBuilder netAmount(BigDecimal netAmount) { this.netAmount = netAmount; return this; }
        public BillResponseBuilder paidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; return this; }
        public BillResponseBuilder dueAmount(BigDecimal dueAmount) { this.dueAmount = dueAmount; return this; }
        public BillResponseBuilder status(String status) { this.status = status; return this; }
        public BillResponseBuilder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public BillResponseBuilder paymentDate(OffsetDateTime paymentDate) { this.paymentDate = paymentDate; return this; }
        public BillResponseBuilder notes(String notes) { this.notes = notes; return this; }
        public BillResponseBuilder serviceRequests(List<BillServiceRequestDetails> serviceRequests) { this.serviceRequests = serviceRequests; return this; }
        public BillResponseBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public BillResponseBuilder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public BillResponse build() {
            return new BillResponse(id, invoiceNumber, invoiceDate, dueDate, encounterId, encounterLocalValue, patientId, patientMrn, patientName, patientContactPhone, organizationId, organizationName, totalAmount, discountPercentage, discountAmount, netAmount, paidAmount, dueAmount, status, paymentMethod, paymentDate, notes, serviceRequests, createdAt, updatedAt);
        }
    }

    public static class BillServiceRequestDetails {
        private Integer serviceRequestId;
        private String serviceRequestLocalValue;
        private String status; 
        private String priority;
        private List<ServiceRequestResponse.TestDetailsResponse> requestedTests;

        public BillServiceRequestDetails() {}

        public BillServiceRequestDetails(Integer serviceRequestId, String serviceRequestLocalValue, String status, String priority, List<ServiceRequestResponse.TestDetailsResponse> requestedTests) {
            this.serviceRequestId = serviceRequestId;
            this.serviceRequestLocalValue = serviceRequestLocalValue;
            this.status = status;
            this.priority = priority;
            this.requestedTests = requestedTests;
        }

        public static BillServiceRequestDetailsBuilder builder() {
            return new BillServiceRequestDetailsBuilder();
        }

        // Getters and Setters
        public Integer getServiceRequestId() { return serviceRequestId; }
        public void setServiceRequestId(Integer serviceRequestId) { this.serviceRequestId = serviceRequestId; }

        public String getServiceRequestLocalValue() { return serviceRequestLocalValue; }
        public void setServiceRequestLocalValue(String serviceRequestLocalValue) { this.serviceRequestLocalValue = serviceRequestLocalValue; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public List<ServiceRequestResponse.TestDetailsResponse> getRequestedTests() { return requestedTests; }
        public void setRequestedTests(List<ServiceRequestResponse.TestDetailsResponse> requestedTests) { this.requestedTests = requestedTests; }

        public static class BillServiceRequestDetailsBuilder {
            private Integer serviceRequestId;
            private String serviceRequestLocalValue;
            private String status;
            private String priority;
            private List<ServiceRequestResponse.TestDetailsResponse> requestedTests;

            public BillServiceRequestDetailsBuilder serviceRequestId(Integer serviceRequestId) { this.serviceRequestId = serviceRequestId; return this; }
            public BillServiceRequestDetailsBuilder serviceRequestLocalValue(String serviceRequestLocalValue) { this.serviceRequestLocalValue = serviceRequestLocalValue; return this; }
            public BillServiceRequestDetailsBuilder status(String status) { this.status = status; return this; }
            public BillServiceRequestDetailsBuilder priority(String priority) { this.priority = priority; return this; }
            public BillServiceRequestDetailsBuilder requestedTests(List<ServiceRequestResponse.TestDetailsResponse> requestedTests) { this.requestedTests = requestedTests; return this; }

            public BillServiceRequestDetails build() {
                return new BillServiceRequestDetails(serviceRequestId, serviceRequestLocalValue, status, priority, requestedTests);
            }
        }
    }
}

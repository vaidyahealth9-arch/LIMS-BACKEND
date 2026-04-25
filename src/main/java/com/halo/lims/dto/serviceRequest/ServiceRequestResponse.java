package com.halo.lims.dto.serviceRequest;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceRequestResponse {
    private Integer id;
    private String localOrderValue;
    private Integer patientId;
    private String patientMrn;
    private String patientName;
    private Integer requesterId;
    private String requesterName;
    private Integer encounterId;
    private String encounterLocalValue;
    private OffsetDateTime orderDate;
    private String status;
    private String priority;
    private Integer organizationId; 
    private String organizationName;
    private List<TestDetailsResponse> requestedTests; 
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public ServiceRequestResponse() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLocalOrderValue() { return localOrderValue; }
    public void setLocalOrderValue(String localOrderValue) { this.localOrderValue = localOrderValue; }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public String getPatientMrn() { return patientMrn; }
    public void setPatientMrn(String patientMrn) { this.patientMrn = patientMrn; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Integer getRequesterId() { return requesterId; }
    public void setRequesterId(Integer requesterId) { this.requesterId = requesterId; }

    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public Integer getEncounterId() { return encounterId; }
    public void setEncounterId(Integer encounterId) { this.encounterId = encounterId; }

    public String getEncounterLocalValue() { return encounterLocalValue; }
    public void setEncounterLocalValue(String encounterLocalValue) { this.encounterLocalValue = encounterLocalValue; }

    public OffsetDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(OffsetDateTime orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public List<TestDetailsResponse> getRequestedTests() { return requestedTests; }
    public void setRequestedTests(List<TestDetailsResponse> requestedTests) { this.requestedTests = requestedTests; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TestDetailsResponse {
        private Integer testId;
        private String testLocalCode;
        private String testName;
        private String status; 
        private BigDecimal price; 
        private List<String> specimenBarcodes;

        public TestDetailsResponse() {}

        // Getters and Setters
        public Integer getTestId() { return testId; }
        public void setTestId(Integer testId) { this.testId = testId; }

        public String getTestLocalCode() { return testLocalCode; }
        public void setTestLocalCode(String testLocalCode) { this.testLocalCode = testLocalCode; }

        public String getTestName() { return testName; }
        public void setTestName(String testName) { this.testName = testName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public List<String> getSpecimenBarcodes() { return specimenBarcodes; }
        public void setSpecimenBarcodes(List<String> specimenBarcodes) { this.specimenBarcodes = specimenBarcodes; }
    }
}

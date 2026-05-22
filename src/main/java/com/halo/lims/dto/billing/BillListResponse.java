package com.halo.lims.dto.billing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class BillListResponse {
    private Integer billId;
    private String invoiceNumber;
    private OffsetDateTime invoiceDate;
    private String patientName;
    private String patientMrn;
    private Integer encounterId;
    private String localEncounterId;
    private java.math.BigDecimal totalAmount;
    private java.math.BigDecimal discountAmount;
    private BigDecimal netAmount;
    private BigDecimal paidAmount;
    private java.math.BigDecimal discountPercentage;
    private String status;
    private List<Integer> serviceRequestIds;
    private List<String> tests;
    private List<TestItem> testItems;
    private String encounterStatus;

    public static class TestItem {
        private String testName;
        private java.math.BigDecimal price;

        public TestItem(String testName, java.math.BigDecimal price) {
            this.testName = testName;
            this.price = price;
        }

        public String getTestName() { return testName; }
        public java.math.BigDecimal getPrice() { return price; }
    }

    public BillListResponse(Integer billId, String invoiceNumber, OffsetDateTime invoiceDate, String patientName, String patientMrn, Integer encounterId, String localEncounterId, java.math.BigDecimal totalAmount, java.math.BigDecimal discountAmount, java.math.BigDecimal netAmount, java.math.BigDecimal paidAmount, java.math.BigDecimal discountPercentage, String status, List<Integer> serviceRequestIds, List<String> tests, List<TestItem> testItems, String encounterStatus) {
        this.billId = billId;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.patientName = patientName;
        this.patientMrn = patientMrn;
        this.encounterId = encounterId;
        this.localEncounterId = localEncounterId;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.netAmount = netAmount;
        this.paidAmount = paidAmount;
        this.discountPercentage = discountPercentage;
        this.status = status;
        this.serviceRequestIds = serviceRequestIds;
        this.tests = tests;
        this.testItems = testItems;
        this.encounterStatus = encounterStatus;
    }

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public OffsetDateTime getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(OffsetDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientMrn() {
        return patientMrn;
    }

    public void setPatientMrn(String patientMrn) {
        this.patientMrn = patientMrn;
    }

    public String getLocalEncounterId() {
        return localEncounterId;
    }

    public void setLocalEncounterId(String localEncounterId) {
        this.localEncounterId = localEncounterId;
    }

    public Integer getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(Integer encounterId) {
        this.encounterId = encounterId;
    }

    public java.math.BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(java.math.BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public java.math.BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(java.math.BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public java.math.BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(java.math.BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public java.math.BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(java.math.BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public java.math.BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(java.math.BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Integer> getServiceRequestIds() {
        return serviceRequestIds;
    }

    public void setServiceRequestIds(List<Integer> serviceRequestIds) {
        this.serviceRequestIds = serviceRequestIds;
    }

    public List<String> getTests() {
        return tests;
    }

    public void setTests(List<String> tests) {
        this.tests = tests;
    }

    public List<TestItem> getTestItems() {
        return testItems;
    }

    public void setTestItems(List<TestItem> testItems) {
        this.testItems = testItems;
    }

    public String getEncounterStatus() {
        return encounterStatus;
    }

    public void setEncounterStatus(String encounterStatus) {
        this.encounterStatus = encounterStatus;
    }
}

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
    private String localEncounterId;
    private BigDecimal netAmount;
    private BigDecimal paidAmount;
    private String status;
    private List<Integer> serviceRequestIds;
    private List<String> tests;

    public BillListResponse(Integer billId, String invoiceNumber, OffsetDateTime invoiceDate, String patientName, String patientMrn, String localEncounterId, BigDecimal netAmount, BigDecimal paidAmount, String status, List<Integer> serviceRequestIds, List<String> tests) {
        this.billId = billId;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.patientName = patientName;
        this.patientMrn = patientMrn;
        this.localEncounterId = localEncounterId;
        this.netAmount = netAmount;
        this.paidAmount = paidAmount;
        this.status = status;
        this.serviceRequestIds = serviceRequestIds;
        this.tests = tests;
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

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
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
}

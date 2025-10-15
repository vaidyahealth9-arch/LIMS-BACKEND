package com.halo.lims.dto.billing;

import java.util.List;

public class BillableServiceRequest {
    private Integer serviceRequestId;
    private List<BillableTest> tests;

    public BillableServiceRequest(Integer serviceRequestId, List<BillableTest> tests) {
        this.serviceRequestId = serviceRequestId;
        this.tests = tests;
    }

    public Integer getServiceRequestId() {
        return serviceRequestId;
    }

    public void setServiceRequestId(Integer serviceRequestId) {
        this.serviceRequestId = serviceRequestId;
    }

    public List<BillableTest> getTests() {
        return tests;
    }

    public void setTests(List<BillableTest> tests) {
        this.tests = tests;
    }
}

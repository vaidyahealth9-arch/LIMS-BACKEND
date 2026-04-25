package com.halo.lims.model.compositeKeys;

import com.halo.lims.model.ServiceRequest;
import com.halo.lims.model.Test;
import java.io.Serializable;
import java.util.Objects;

public class ServiceRequestItemId implements Serializable {
    private ServiceRequest serviceRequest;
    private Test test;

    public ServiceRequestItemId() {}

    public ServiceRequestItemId(ServiceRequest serviceRequest, Test test) {
        this.serviceRequest = serviceRequest;
        this.test = test;
    }

    // Getters and Setters
    public ServiceRequest getServiceRequest() { return serviceRequest; }
    public void setServiceRequest(ServiceRequest serviceRequest) { this.serviceRequest = serviceRequest; }

    public Test getTest() { return test; }
    public void setTest(Test test) { this.test = test; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceRequestItemId that = (ServiceRequestItemId) o;
        return Objects.equals(serviceRequest, that.serviceRequest) && Objects.equals(test, that.test);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceRequest, test);
    }
}

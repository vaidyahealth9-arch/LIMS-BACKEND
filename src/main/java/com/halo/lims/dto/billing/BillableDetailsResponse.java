package com.halo.lims.dto.billing;

import java.util.List;

public class BillableDetailsResponse {
    private String localEncounterId;
    private List<BillableServiceRequest> serviceRequests;

    public BillableDetailsResponse(String localEncounterId, List<BillableServiceRequest> serviceRequests) {
        this.localEncounterId = localEncounterId;
        this.serviceRequests = serviceRequests;
    }

    public String getLocalEncounterId() {
        return localEncounterId;
    }

    public void setLocalEncounterId(String localEncounterId) {
        this.localEncounterId = localEncounterId;
    }

    public List<BillableServiceRequest> getServiceRequests() {
        return serviceRequests;
    }

    public void setServiceRequests(List<BillableServiceRequest> serviceRequests) {
        this.serviceRequests = serviceRequests;
    }
}

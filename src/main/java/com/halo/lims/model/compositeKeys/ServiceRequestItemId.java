package com.halo.lims.model.compositeKeys;

import com.halo.lims.model.ServiceRequest;
import com.halo.lims.model.Test;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// Composite Primary Key class for ServiceRequestItem
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestItemId implements Serializable {
    private ServiceRequest serviceRequest;
    private Test test;
}

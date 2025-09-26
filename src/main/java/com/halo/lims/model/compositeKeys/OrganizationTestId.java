package com.halo.lims.model.compositeKeys;

import com.halo.lims.model.Organization;
import com.halo.lims.model.Test;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

// Public class for composite primary key
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationTestId implements Serializable {
    private Organization organization;
    private Test test;
}
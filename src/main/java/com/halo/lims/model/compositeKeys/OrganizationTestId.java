package com.halo.lims.model.compositeKeys;

import com.halo.lims.model.Organization;
import com.halo.lims.model.Test;
import java.io.Serializable;
import java.util.Objects;

public class OrganizationTestId implements Serializable {
    private Organization organization;
    private Test test;

    public OrganizationTestId() {}

    public OrganizationTestId(Organization organization, Test test) {
        this.organization = organization;
        this.test = test;
    }

    // Getters and Setters
    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public Test getTest() { return test; }
    public void setTest(Test test) { this.test = test; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationTestId that = (OrganizationTestId) o;
        return Objects.equals(organization, that.organization) && Objects.equals(test, that.test);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organization, test);
    }
}
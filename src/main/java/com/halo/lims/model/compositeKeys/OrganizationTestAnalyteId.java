package com.halo.lims.model.compositeKeys;

import java.io.Serializable;
import java.util.Objects;

public class OrganizationTestAnalyteId implements Serializable {
    private Integer organization;
    private Integer testAnalyte;

    public OrganizationTestAnalyteId() {}

    public OrganizationTestAnalyteId(Integer organization, Integer testAnalyte) {
        this.organization = organization;
        this.testAnalyte = testAnalyte;
    }

    // Getters and Setters
    public Integer getOrganization() { return organization; }
    public void setOrganization(Integer organization) { this.organization = organization; }

    public Integer getTestAnalyte() { return testAnalyte; }
    public void setTestAnalyte(Integer testAnalyte) { this.testAnalyte = testAnalyte; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationTestAnalyteId that = (OrganizationTestAnalyteId) o;
        return Objects.equals(organization, that.organization) && Objects.equals(testAnalyte, that.testAnalyte);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organization, testAnalyte);
    }
}

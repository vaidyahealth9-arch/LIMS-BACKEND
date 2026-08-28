package com.halo.lims.dto.organization.test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class OrganizationTestResponse {
    private Integer organizationId;
    private String organizationName;
    private Integer testId;
    private String testLocalCode;
    private String testName;
    private Boolean isEnabled;
    private BigDecimal price;
    private Integer specimenTypeId;
    private String specimenTypeName;
    private Integer defaultNumberOfSpecimens;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public OrganizationTestResponse() {}

    // Getters and Setters
    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public Integer getTestId() { return testId; }
    public void setTestId(Integer testId) { this.testId = testId; }

    public String getTestLocalCode() { return testLocalCode; }
    public void setTestLocalCode(String testLocalCode) { this.testLocalCode = testLocalCode; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getSpecimenTypeId() { return specimenTypeId; }
    public void setSpecimenTypeId(Integer specimenTypeId) { this.specimenTypeId = specimenTypeId; }

    public String getSpecimenTypeName() { return specimenTypeName; }
    public void setSpecimenTypeName(String specimenTypeName) { this.specimenTypeName = specimenTypeName; }

    public Integer getDefaultNumberOfSpecimens() { return defaultNumberOfSpecimens; }
    public void setDefaultNumberOfSpecimens(Integer defaultNumberOfSpecimens) { this.defaultNumberOfSpecimens = defaultNumberOfSpecimens; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}


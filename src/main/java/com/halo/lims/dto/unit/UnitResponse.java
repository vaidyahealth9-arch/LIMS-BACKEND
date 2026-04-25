package com.halo.lims.dto.unit;

import java.time.OffsetDateTime;

public class UnitResponse {
    private Integer id;
    private String name;
    private String ucumCode;
    private String description;
    private Integer organizationId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public UnitResponse() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUcumCode() { return ucumCode; }
    public void setUcumCode(String ucumCode) { this.ucumCode = ucumCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

package com.halo.lims.dto.unit;

import jakarta.validation.constraints.Size;

public class UnitUpdateRequest {
    private Integer organizationId;

    @Size(max = 50)
    private String name; 

    private String description;

    public UnitUpdateRequest() {}

    // Getters and Setters
    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

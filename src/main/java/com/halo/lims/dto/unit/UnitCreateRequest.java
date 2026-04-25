package com.halo.lims.dto.unit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UnitCreateRequest {
    private Integer organizationId;

    @NotBlank(message = "Unit name is required")
    @Size(max = 50)
    private String name;

    @NotBlank(message = "UCUM Code is required")
    @Size(max = 50)
    private String ucumCode;

    private String description; 

    public UnitCreateRequest() {}

    // Getters and Setters
    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUcumCode() { return ucumCode; }
    public void setUcumCode(String ucumCode) { this.ucumCode = ucumCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

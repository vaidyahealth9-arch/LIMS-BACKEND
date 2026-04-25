package com.halo.lims.dto.specimenType;

import java.time.OffsetDateTime;

public class SpecimenTypeResponse {
    private Integer id;
    private String name;
    private String snomedCode;
    private String snomedSystem;
    private String description;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public SpecimenTypeResponse() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSnomedCode() { return snomedCode; }
    public void setSnomedCode(String snomedCode) { this.snomedCode = snomedCode; }

    public String getSnomedSystem() { return snomedSystem; }
    public void setSnomedSystem(String snomedSystem) { this.snomedSystem = snomedSystem; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
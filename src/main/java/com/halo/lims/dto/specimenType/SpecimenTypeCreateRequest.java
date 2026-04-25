package com.halo.lims.dto.specimenType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SpecimenTypeCreateRequest {
    @NotBlank(message = "Specimen type name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 50)
    private String snomedCode;

    @Size(max = 255)
    private String snomedSystem; 

    private String description; 

    public SpecimenTypeCreateRequest() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSnomedCode() { return snomedCode; }
    public void setSnomedCode(String snomedCode) { this.snomedCode = snomedCode; }

    public String getSnomedSystem() { return snomedSystem; }
    public void setSnomedSystem(String snomedSystem) { this.snomedSystem = snomedSystem; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

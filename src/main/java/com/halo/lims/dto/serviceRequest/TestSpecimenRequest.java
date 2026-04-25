package com.halo.lims.dto.serviceRequest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TestSpecimenRequest {
    @NotNull
    @Min(1)
    private Integer testId;

    @NotNull
    @Min(1)
    private Integer specimenTypeId;

    @NotNull
    @Min(1)
    private Integer numberOfSpecimens;

    public TestSpecimenRequest() {}

    // Getters and Setters
    public Integer getTestId() { return testId; }
    public void setTestId(Integer testId) { this.testId = testId; }

    public Integer getSpecimenTypeId() { return specimenTypeId; }
    public void setSpecimenTypeId(Integer specimenTypeId) { this.specimenTypeId = specimenTypeId; }

    public Integer getNumberOfSpecimens() { return numberOfSpecimens; }
    public void setNumberOfSpecimens(Integer numberOfSpecimens) { this.numberOfSpecimens = numberOfSpecimens; }
}

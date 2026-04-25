package com.halo.lims.dto.test;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class ReferenceRangeUpdateRequest {
    @Pattern(regexp = "male|female|other|unknown|null", message = "Gender must be male, female, other, unknown, or null")
    private String gender;

    @Min(value = 0, message = "Minimum age must be non-negative")
    private Integer minAgeYears;

    @Min(value = 0, message = "Maximum age must be non-negative")
    private Integer maxAgeYears;

    @DecimalMin(value = "0.0", inclusive = false, message = "Low value must be positive")
    private BigDecimal lowValue;

    @DecimalMin(value = "0.0", inclusive = false, message = "High value must be positive")
    private BigDecimal highValue;

    @Size(max = 255)
    private String textRange;

    @Size(max = 50)
    private String interpretationCode;

    public ReferenceRangeUpdateRequest() {}

    // Getters and Setters
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getMinAgeYears() { return minAgeYears; }
    public void setMinAgeYears(Integer minAgeYears) { this.minAgeYears = minAgeYears; }

    public Integer getMaxAgeYears() { return maxAgeYears; }
    public void setMaxAgeYears(Integer maxAgeYears) { this.maxAgeYears = maxAgeYears; }

    public BigDecimal getLowValue() { return lowValue; }
    public void setLowValue(BigDecimal lowValue) { this.lowValue = lowValue; }

    public BigDecimal getHighValue() { return highValue; }
    public void setHighValue(BigDecimal highValue) { this.highValue = highValue; }

    public String getTextRange() { return textRange; }
    public void setTextRange(String textRange) { this.textRange = textRange; }

    public String getInterpretationCode() { return interpretationCode; }
    public void setInterpretationCode(String interpretationCode) { this.interpretationCode = interpretationCode; }
}
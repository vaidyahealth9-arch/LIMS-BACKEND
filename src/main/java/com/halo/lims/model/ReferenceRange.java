package com.halo.lims.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reference_ranges")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferenceRange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analyte_id", nullable = false)
    private TestAnalyte analyte; // FK to the specific analyte

    @Column(length = 10)
    private String gender; // "male", "female", "other", "unknown", NULL for all

    @Column(name = "min_age_years")
    private Integer minAgeYears;

    @Column(name = "max_age_years")
    private Integer maxAgeYears;

    @Column(name = "low_value", precision = 10, scale = 4) // Example precision/scale
    private BigDecimal lowValue;

    @Column(name = "high_value", precision = 10, scale = 4)
    private BigDecimal highValue;

    @Column(name = "text_range", length = 255)
    private String textRange;

    @Column(name = "interpretation_code", length = 50)
    private String interpretationCode; // e.g., "N", "H", "L" (from ObservationInterpretation)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public TestAnalyte getAnalyte() { return analyte; }
    public void setAnalyte(TestAnalyte analyte) { this.analyte = analyte; }

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

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
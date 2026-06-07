package com.halo.lims.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "test_analytes")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAnalyte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "analyte_code", unique = true, nullable = false, length = 100)
    private String analyteCode;

    @Column(name = "analyte_name", nullable = false)
    private String analyteName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_test_id", nullable = false)
    private Test parentTest;

    @Column(name = "loinc_code", unique = true, length = 50)
    private String loincCode;

    @Column(name = "loinc_system", length = 255)
    private String loincSystem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(name = "result_type", nullable = false, length = 50)
    private String resultType; // e.g., "Numeric", "Text", "Coded"

    @Column(name = "decimal_places")
    private Integer decimalPlaces;

    @Column(name = "biological_ref_interval", columnDefinition = "TEXT")
    private String biologicalRefInterval;

    @Column(name = "is_derived", nullable = false)
    private Boolean isDerived;

    @Column(columnDefinition = "TEXT")
    private String formula;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "method", length = 255)
    private String method;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getAnalyteCode() { return analyteCode; }
    public void setAnalyteCode(String analyteCode) { this.analyteCode = analyteCode; }

    public String getAnalyteName() { return analyteName; }
    public void setAnalyteName(String analyteName) { this.analyteName = analyteName; }

    public Test getParentTest() { return parentTest; }
    public void setParentTest(Test parentTest) { this.parentTest = parentTest; }

    public String getLoincCode() { return loincCode; }
    public void setLoincCode(String loincCode) { this.loincCode = loincCode; }

    public String getLoincSystem() { return loincSystem; }
    public void setLoincSystem(String loincSystem) { this.loincSystem = loincSystem; }

    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }

    public String getResultType() { return resultType; }
    public void setResultType(String resultType) { this.resultType = resultType; }

    public Integer getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(Integer decimalPlaces) { this.decimalPlaces = decimalPlaces; }

    public String getBiologicalRefInterval() { return biologicalRefInterval; }
    public void setBiologicalRefInterval(String biologicalRefInterval) { this.biologicalRefInterval = biologicalRefInterval; }

    public Boolean getIsDerived() { return isDerived; }
    public void setIsDerived(Boolean isDerived) { this.isDerived = isDerived; }

    public String getFormula() { return formula; }
    public void setFormula(String formula) { this.formula = formula; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

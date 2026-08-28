package com.halo.lims.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import com.halo.lims.model.Organization;


@Entity
@Table(name = "tests")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "test_name", nullable = false)
    private String testName;

    @Column(name = "local_code", unique = true, nullable = false, length = 100)
    private String localCode;

    @Column(name = "loinc_code", unique = true, length = 50)
    private String loincCode;

    @Column(name = "loinc_system", length = 255)
    private String loincSystem;

    @Column(length = 100)
    private String department;

    @Column(name = "container_description", columnDefinition = "TEXT")
    private String containerDescription;

    @Column(length = 255)
    private String method;

    @Column(name = "measuring_principle", columnDefinition = "TEXT")
    private String measuringPrinciple;

    @Column(name = "turn_around_time_text", length = 255)
    private String turnAroundTimeText;

    @Column(name = "reflex_profile_text", columnDefinition = "TEXT")
    private String reflexProfileText;

    @Column(name = "report_notes", columnDefinition = "TEXT")
    private String reportNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getLocalCode() { return localCode; }
    public void setLocalCode(String localCode) { this.localCode = localCode; }

    public String getLoincCode() { return loincCode; }
    public void setLoincCode(String loincCode) { this.loincCode = loincCode; }

    public String getLoincSystem() { return loincSystem; }
    public void setLoincSystem(String loincSystem) { this.loincSystem = loincSystem; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getContainerDescription() { return containerDescription; }
    public void setContainerDescription(String containerDescription) { this.containerDescription = containerDescription; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getMeasuringPrinciple() { return measuringPrinciple; }
    public void setMeasuringPrinciple(String measuringPrinciple) { this.measuringPrinciple = measuringPrinciple; }

    public String getTurnAroundTimeText() { return turnAroundTimeText; }
    public void setTurnAroundTimeText(String turnAroundTimeText) { this.turnAroundTimeText = turnAroundTimeText; }

    public String getReflexProfileText() { return reflexProfileText; }
    public void setReflexProfileText(String reflexProfileText) { this.reflexProfileText = reflexProfileText; }

    public String getReportNotes() { return reportNotes; }
    public void setReportNotes(String reportNotes) { this.reportNotes = reportNotes; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

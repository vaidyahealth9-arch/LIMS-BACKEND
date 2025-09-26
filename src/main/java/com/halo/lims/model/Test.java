package com.halo.lims.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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
}

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
@Table(name = "organization_test_interpretation_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationTestInterpretationRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "organization_id", referencedColumnName = "organization_id"),
        @JoinColumn(name = "test_id", referencedColumnName = "test_id")
    })
    private OrganizationTest organizationTest;

    @Column(name = "condition_expression", nullable = false, columnDefinition = "TEXT")
    private String conditionExpression;

    @Column(length = 100)
    private String classification; // e.g., "Normal", "Mild prolongation"

    @Column(name = "auto_comment", columnDefinition = "TEXT")
    private String autoComment;

    @Column(name = "reflex_action_text", columnDefinition = "TEXT")
    private String reflexActionText;

    @Column(length = 50)
    private String priority; // "Info", "Routine", "Priority", "STAT", "Critical"

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

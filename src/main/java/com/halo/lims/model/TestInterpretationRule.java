package com.halo.lims.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "test_interpretation_rules")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestInterpretationRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "rule_id", unique = true, nullable = false, length = 100)
    private String ruleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analyte_id", nullable = false)
    private TestAnalyte analyte;

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

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public TestAnalyte getAnalyte() { return analyte; }
    public void setAnalyte(TestAnalyte analyte) { this.analyte = analyte; }

    public String getConditionExpression() { return conditionExpression; }
    public void setConditionExpression(String conditionExpression) { this.conditionExpression = conditionExpression; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public String getAutoComment() { return autoComment; }
    public void setAutoComment(String autoComment) { this.autoComment = autoComment; }

    public String getReflexActionText() { return reflexActionText; }
    public void setReflexActionText(String reflexActionText) { this.reflexActionText = reflexActionText; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

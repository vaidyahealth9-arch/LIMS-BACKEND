package com.halo.lims.model;

import com.halo.lims.model.compositeKeys.PanelTestId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "panel_tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PanelTestId.class) // Define composite primary key
public class PanelTest {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panel_id")
    private TestPanel panel;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id")
    private Test test; // This links to an orderable test

    @Column(name = "display_order")
    private Integer displayOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}


package com.halo.lims.model;

import com.halo.lims.model.compositeKeys.ServiceRequestItemId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "service_request_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ServiceRequestItemId.class)
public class ServiceRequestItem {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id")
    private ServiceRequest serviceRequest;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id")
    private Test test; // This links to an orderable test

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panel_id")
    private TestPanel panel;

    @Column(length = 50)
    private String status; // Status of this specific test/panel within the order

    @Column(name = "barcode", columnDefinition = "TEXT")
    private String barcode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}


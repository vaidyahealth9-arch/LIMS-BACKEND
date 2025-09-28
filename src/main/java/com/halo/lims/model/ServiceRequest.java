package com.halo.lims.model;

import com.halo.lims.constant.RequestPriority;
import com.halo.lims.constant.ServiceRequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "service_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Practitioner requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id")
    private Encounter encounter;

    @Column(name = "order_date", nullable = false)
    private OffsetDateTime orderDate;

    @Column(nullable = false, length = 50)
    private String status = ServiceRequestStatus.DRAFT.getCode(); // FHIR ServiceRequestStatus

    @Column(length = 20)
    private String priority = RequestPriority.ROUTINE.getCode(); // FHIR RequestPriority

    @Column(name = "local_order_system", nullable = false, length = 255)
    private String localOrderSystem;

    @Column(name = "local_order_value", unique = true, nullable = false, length = 255)
    private String localOrderValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

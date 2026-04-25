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
@Builder
@AllArgsConstructor
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

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String status = ServiceRequestStatus.DRAFT.getCode(); // FHIR ServiceRequestStatus

    @Builder.Default
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

    public ServiceRequest() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Practitioner getRequester() { return requester; }
    public void setRequester(Practitioner requester) { this.requester = requester; }

    public Encounter getEncounter() { return encounter; }
    public void setEncounter(Encounter encounter) { this.encounter = encounter; }

    public OffsetDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(OffsetDateTime orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getLocalOrderSystem() { return localOrderSystem; }
    public void setLocalOrderSystem(String localOrderSystem) { this.localOrderSystem = localOrderSystem; }

    public String getLocalOrderValue() { return localOrderValue; }
    public void setLocalOrderValue(String localOrderValue) { this.localOrderValue = localOrderValue; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}

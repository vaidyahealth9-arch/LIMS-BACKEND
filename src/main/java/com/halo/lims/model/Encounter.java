package com.halo.lims.model;

import com.halo.lims.constant.EncounterClass;
import com.halo.lims.constant.EncounterStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "encounters")
@Builder
@AllArgsConstructor
public class Encounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String status = EncounterStatus.PLANNED.getCode();

    @Builder.Default
    @Column(name = "encounter_class", length = 100)
    private String encounterClass = EncounterClass.AMBULATORY.getCode();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_provider_id")
    private Organization serviceProvider;

    @Column(name = "ref_doctor", length = 100)
    private String referenceDoctor;

    @Column(name = "local_encounter_system", nullable = false, length = 255)
    private String localEncounterSystem;

    @Column(name = "local_encounter_value", unique = true, nullable = false, length = 255)
    private String localEncounterValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approving_practitioner_id")
    private Practitioner approvingPractitioner;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Encounter() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }

    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEncounterClass() { return encounterClass; }
    public void setEncounterClass(String encounterClass) { this.encounterClass = encounterClass; }

    public Organization getServiceProvider() { return serviceProvider; }
    public void setServiceProvider(Organization serviceProvider) { this.serviceProvider = serviceProvider; }

    public String getReferenceDoctor() { return referenceDoctor; }
    public void setReferenceDoctor(String referenceDoctor) { this.referenceDoctor = referenceDoctor; }

    public String getLocalEncounterSystem() { return localEncounterSystem; }
    public void setLocalEncounterSystem(String localEncounterSystem) { this.localEncounterSystem = localEncounterSystem; }

    public String getLocalEncounterValue() { return localEncounterValue; }
    public void setLocalEncounterValue(String localEncounterValue) { this.localEncounterValue = localEncounterValue; }

    public Practitioner getApprovingPractitioner() { return approvingPractitioner; }
    public void setApprovingPractitioner(Practitioner approvingPractitioner) { this.approvingPractitioner = approvingPractitioner; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}

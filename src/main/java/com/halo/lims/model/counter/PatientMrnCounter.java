package com.halo.lims.model.counter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "patient_mrn_counters")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientMrnCounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "organization_id", nullable = false, unique = true)
    private Integer organizationId;

    @Column(name = "next_counter", nullable = false)
    private Long nextCounter;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public Long getNextCounter() { return nextCounter; }
    public void setNextCounter(Long nextCounter) { this.nextCounter = nextCounter; }
}
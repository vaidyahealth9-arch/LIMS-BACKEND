package com.halo.lims.model.counter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "practitioner_counters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PractitionerCounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "organization_id", nullable = false, unique = true)
    private Integer organizationId;

    @Column(name = "next_counter", nullable = false)
    private Long nextCounter;
}

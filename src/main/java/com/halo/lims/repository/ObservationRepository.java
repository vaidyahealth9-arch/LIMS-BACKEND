package com.halo.lims.repository;

import com.halo.lims.model.Observation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObservationRepository extends JpaRepository<Observation, Integer> {
    List<Observation> findByServiceRequestId(Integer serviceRequestId);

    Optional<Observation> findTopByServiceRequestIdOrderByEffectiveDateTimeDesc(Integer serviceRequestId);
}

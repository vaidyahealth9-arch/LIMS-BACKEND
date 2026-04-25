package com.halo.lims.repository;

import com.halo.lims.model.counter.ObservationCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ObservationCounterRepository extends JpaRepository<ObservationCounter, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ObservationCounter c where c.organizationId = :organizationId")
    Optional<ObservationCounter> findByOrganizationIdForUpdate(@Param("organizationId") Integer organizationId);
}

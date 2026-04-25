package com.halo.lims.repository;

import com.halo.lims.model.counter.EncounterValueCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface EncounterValueCounterRepository extends JpaRepository<EncounterValueCounter, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from EncounterValueCounter c where c.organizationId = :organizationId")
    Optional<EncounterValueCounter> findByOrganizationIdForUpdate(@Param("organizationId") Integer organizationId);
}
package com.halo.lims.repository;

import com.halo.lims.model.counter.SpecimenValueCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface SpecimenValueCounterRepository extends JpaRepository<SpecimenValueCounter, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from SpecimenValueCounter c where c.organizationId = :organizationId")
    Optional<SpecimenValueCounter> findByOrganizationIdForUpdate(@Param("organizationId") Integer organizationId);
}
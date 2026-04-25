package com.halo.lims.repository;

import com.halo.lims.model.counter.PatientMrnCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface PatientMrnCounterRepository extends JpaRepository<PatientMrnCounter, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from PatientMrnCounter c where c.organizationId = :organizationId")
    Optional<PatientMrnCounter> findByOrganizationIdForUpdate(@Param("organizationId") Integer organizationId);
}
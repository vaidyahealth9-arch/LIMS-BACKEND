package com.halo.lims.repository;

import com.halo.lims.model.counter.DiagnosticReportCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface DiagnosticReportCounterRepository extends JpaRepository<DiagnosticReportCounter, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from DiagnosticReportCounter c where c.organizationId = :organizationId")
    Optional<DiagnosticReportCounter> findByOrganizationIdForUpdate(@Param("organizationId") Integer organizationId);
}
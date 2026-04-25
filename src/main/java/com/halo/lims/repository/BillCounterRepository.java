package com.halo.lims.repository;

import com.halo.lims.model.counter.BillCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface BillCounterRepository extends JpaRepository<BillCounter, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from BillCounter c where c.organizationId = :organizationId")
    Optional<BillCounter> findByOrganizationIdForUpdate(@Param("organizationId") Integer organizationId);
}
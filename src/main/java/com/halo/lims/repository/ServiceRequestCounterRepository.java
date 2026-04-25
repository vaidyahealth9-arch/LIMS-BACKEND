package com.halo.lims.repository;

import com.halo.lims.model.counter.ServiceRequestCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ServiceRequestCounterRepository extends JpaRepository<ServiceRequestCounter, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ServiceRequestCounter c where c.organizationId = :organizationId")
    Optional<ServiceRequestCounter> findByOrganizationIdForUpdate(@Param("organizationId") Integer organizationId);
}
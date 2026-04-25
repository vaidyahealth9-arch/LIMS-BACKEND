package com.halo.lims.repository;

import com.halo.lims.model.counter.PractitionerCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface PractitionerCounterRepository extends JpaRepository<PractitionerCounter, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from PractitionerCounter c where c.organizationId = :organizationId")
    Optional<PractitionerCounter> findByOrganizationIdForUpdate(@Param("organizationId") Integer organizationId);
}

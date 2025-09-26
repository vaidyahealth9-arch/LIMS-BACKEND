package com.halo.lims.repository;

import com.halo.lims.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Integer> {
    Optional<Unit> findByName(String name);
    Optional<Unit> findByUcumCode(String ucumCode);
    boolean existsByName(String name); // NEW
    boolean existsByUcumCode(String ucumCode);
}

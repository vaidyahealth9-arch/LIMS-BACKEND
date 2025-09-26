package com.halo.lims.repository;

import com.halo.lims.model.SpecimenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecimenTypeRepository extends JpaRepository<SpecimenType, Integer> {
    Optional<SpecimenType> findByName(String name);
    Optional<SpecimenType> findBySnomedCode(String snomedCode);
    boolean existsByName(String name); // NEW
    boolean existsBySnomedCode(String snomedCode);
}

package com.halo.lims.repository;

import com.halo.lims.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRepository extends JpaRepository<Test, Integer> {
    Optional<Test> findByLocalCode(String localCode);
    boolean existsByLocalCode(String localCode);
    List<Test> findByTestNameContainingIgnoreCase(String testNamePart); // For search functionality
    List<Test> findByDepartment(String department);
}

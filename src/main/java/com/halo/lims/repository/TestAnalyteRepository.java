package com.halo.lims.repository;

import com.halo.lims.model.TestAnalyte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestAnalyteRepository extends JpaRepository<TestAnalyte, Integer> {
    Optional<TestAnalyte> findByAnalyteCode(String analyteCode);
    List<TestAnalyte> findByParentTestId(Integer parentTestId);
    boolean existsByAnalyteCode(String analyteCode);

    boolean existsByParentTestId(Integer id);

    List<TestAnalyte> findByOrganizationIsNull();
    List<TestAnalyte> findByOrganizationId(Integer organizationId);
}

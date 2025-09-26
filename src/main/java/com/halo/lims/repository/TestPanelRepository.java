package com.halo.lims.repository;

import com.halo.lims.model.TestPanel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestPanelRepository extends JpaRepository<TestPanel, Integer> {
}

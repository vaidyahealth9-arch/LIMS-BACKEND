package com.halo.lims.repository;

import com.halo.lims.model.ReferenceRange;
import com.halo.lims.model.TestAnalyte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferenceRangeRepository extends JpaRepository<ReferenceRange, Integer> {
    List<ReferenceRange> findByAnalyte(TestAnalyte analyte);
    List<ReferenceRange> findByAnalyteId(Integer analyteId);
}

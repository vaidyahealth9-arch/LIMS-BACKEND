package com.halo.lims.repository;

import com.halo.lims.model.DiagnosticReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosticReportRepository extends JpaRepository<DiagnosticReport, Integer> {
}

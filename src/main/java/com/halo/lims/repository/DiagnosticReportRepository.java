package com.halo.lims.repository;

import com.halo.lims.model.DiagnosticReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiagnosticReportRepository extends JpaRepository<DiagnosticReport, Integer> {
	Optional<DiagnosticReport> findByServiceRequest_Id(Integer serviceRequestId);
	Optional<DiagnosticReport> findByLocalReportValue(String localReportValue);
}

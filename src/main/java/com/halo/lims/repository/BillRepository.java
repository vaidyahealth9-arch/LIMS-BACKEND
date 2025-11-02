package com.halo.lims.repository;

import com.halo.lims.model.Bill;
import com.halo.lims.model.Encounter;
import com.halo.lims.model.Organization;
import com.halo.lims.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Integer> {
    Optional<Bill> findByInvoiceNumber(String invoiceNumber);
    List<Bill> findByEncounter(Encounter encounter);
    List<Bill> findByEncounter_Id(Integer encounterId);
    List<Bill> findByPatient(Patient patient);
    List<Bill> findByPatient_Id(Integer patientId);
    List<Bill> findByOrganization(Organization organization);
    List<Bill> findByOrganization_Id(Integer organizationId);
    List<Bill> findByStatus(String status);
    List<Bill> findByOrganization_IdAndStatus(Integer organizationId, String status);

    @Query("SELECT SUM(b.netAmount) FROM Bill b WHERE b.organization.id = :organizationId AND b.invoiceDate BETWEEN :start AND :end")
    BigDecimal sumNetAmountByOrganizationIdAndInvoiceDateBetween(@Param("organizationId") Integer organizationId, @Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    @Query("SELECT FUNCTION('DATE', b.invoiceDate), SUM(b.netAmount) FROM Bill b WHERE b.organization.id = :organizationId AND b.invoiceDate >= :startDate GROUP BY FUNCTION('DATE', b.invoiceDate)")
    List<Object[]> findWeeklyRevenueByOrganization(@Param("organizationId") Integer organizationId, @Param("startDate") OffsetDateTime startDate);

    Page<Bill> findAll(Specification<Bill> spec, Pageable pageable);
}
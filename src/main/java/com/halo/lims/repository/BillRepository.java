package com.halo.lims.repository;

import com.halo.lims.model.Bill;
import com.halo.lims.model.Encounter;
import com.halo.lims.model.Organization;
import com.halo.lims.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

    Page<Bill> findAll(Specification<Bill> spec, Pageable pageable);
}

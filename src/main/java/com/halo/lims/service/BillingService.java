package com.halo.lims.service;

import com.halo.lims.dto.billing.*;
import com.halo.lims.dto.PagedResponse;
import com.halo.lims.dto.serviceRequest.ServiceRequestResponse;
import com.halo.lims.model.*;
import com.halo.lims.repository.*;
import com.halo.lims.security.AesGcmEncryptionUtil;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.IdentifierGenerationService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BillingService {

    private final BillRepository billRepository;
    private final EncounterRepository encounterRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestItemRepository serviceRequestItemRepository;
    private final OrganizationTestRepository organizationTestRepository;
    private final OrganizationRepository organizationRepository;
    private final SecurityService securityService;
    private final AesGcmEncryptionUtil aesGcmEncryptionUtil; // For PII decryption
    private final IdentifierGenerationService identifierGenerationService;

    public BillingService(BillRepository billRepository,
                          EncounterRepository encounterRepository,
                          ServiceRequestRepository serviceRequestRepository,
                          ServiceRequestItemRepository serviceRequestItemRepository,
                          OrganizationTestRepository organizationTestRepository, OrganizationRepository organizationRepository,
                          SecurityService securityService,
                          AesGcmEncryptionUtil aesGcmEncryptionUtil,
                          IdentifierGenerationService identifierGenerationService) {
        this.billRepository = billRepository;
        this.encounterRepository = encounterRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.serviceRequestItemRepository = serviceRequestItemRepository;
        this.organizationTestRepository = organizationTestRepository;
        this.organizationRepository = organizationRepository;
        this.securityService = securityService;
        this.aesGcmEncryptionUtil = aesGcmEncryptionUtil;
        this.identifierGenerationService = identifierGenerationService;
    }

    /**
     * Creates a new bill for an encounter, covering specified service requests.
     * @param request The DTO containing bill details.
     * @return The created BillResponse.
     */
    @Transactional
    public BillResponse createBill(BillCreateRequest request) {
        Encounter encounter = encounterRepository.findById(request.getEncounterId())
                .orElseThrow(() -> new RuntimeException("Encounter not found with ID: " + request.getEncounterId()));

        Patient patient = encounter.getPatient();
        Organization organization = patient.getOrganization();

        // --- Multi-tenancy check ---
        if (!securityService.isUserInOrganization(organization.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to create bills for organization ID: " + organization.getId());
        }
        // --- End multi-tenancy check ---

        List<ServiceRequest> serviceRequests = serviceRequestRepository.findAllById(request.getServiceRequestIds());
        if (serviceRequests.size() != request.getServiceRequestIds().size()) {
            throw new RuntimeException("One or more Service Requests not found.");
        }

        List<Bill> existingBills = billRepository.findByEncounterOrderByCreatedAtAsc(encounter);
        Bill bill = existingBills.isEmpty() ? null : existingBills.get(0);
        boolean updatingExistingBill = bill != null;

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Batch fetch all items and test prices to avoid N+1 queries
        List<ServiceRequestItem> allItems = serviceRequestItemRepository.findByServiceRequestIn(serviceRequests);
        List<Integer> testIds = allItems.stream()
                .map(item -> item.getTest().getId())
                .distinct()
                .toList();

        Map<Integer, OrganizationTest> orgTestMap = organizationTestRepository.findByOrganization_IdAndTest_IdIn(organization.getId(), testIds)
                .stream()
                .collect(Collectors.toMap(ot -> ot.getTest().getId(), ot -> ot));

        for (ServiceRequest sr : serviceRequests) {
            if (!sr.getPatient().getId().equals(patient.getId())) {
                throw new IllegalArgumentException("Service Request " + sr.getId() + " does not belong to patient " + patient.getId());
            }
            if (!sr.getEncounter().getId().equals(encounter.getId())) {
                throw new IllegalArgumentException("Service Request " + sr.getId() + " does not belong to encounter " + encounter.getId());
            }

            List<ServiceRequestItem> srItems = allItems.stream()
                    .filter(item -> item.getServiceRequest().getId().equals(sr.getId()))
                    .toList();

            for (ServiceRequestItem item : srItems) {
                OrganizationTest orgTest = orgTestMap.get(item.getTest().getId());
                if (orgTest == null) {
                    throw new RuntimeException("Test '" + item.getTest().getTestName() + "' is not configured for organization " + organization.getOrganizationName());
                }
                if (!orgTest.getIsEnabled()) {
                    throw new IllegalArgumentException("Test '" + item.getTest().getTestName() + "' is disabled for organization " + organization.getOrganizationName());
                }
                totalAmount = totalAmount.add(orgTest.getPrice());
            }
        }

        // Apply discount
        BigDecimal discountPercentage = request.getDiscountPercentage() != null ? request.getDiscountPercentage() : BigDecimal.ZERO;
        BigDecimal discountFactor = discountPercentage.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = totalAmount.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAmount = totalAmount.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        BigDecimal initialPaidAmount;
        if (bill != null) {
            initialPaidAmount = bill.getPaidAmount() != null ? bill.getPaidAmount() : BigDecimal.ZERO;
        } else {
            initialPaidAmount = request.getInitialPaidAmount() != null ? request.getInitialPaidAmount() : BigDecimal.ZERO;
        }
        if (!updatingExistingBill && initialPaidAmount.compareTo(netAmount) > 0) {
            throw new IllegalArgumentException("Initial paid amount cannot exceed net amount.");
        }
        if (updatingExistingBill && initialPaidAmount.compareTo(netAmount) > 0) {
            throw new IllegalStateException("Existing paid amount exceeds the recalculated net amount. Please review the payment before updating the bill.");
        }
        BigDecimal dueAmount = netAmount.subtract(initialPaidAmount).setScale(2, RoundingMode.HALF_UP);

        String status;
        String paymentMethod = bill != null ? bill.getPaymentMethod() : request.getInitialPaymentMethod();
        if (dueAmount.compareTo(BigDecimal.ZERO) == 0) {
            status = "PAID";
            paymentMethod = paymentMethod != null ? paymentMethod : "NONE"; // If full amount paid but method not specified
        } else if (initialPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            status = "PARTIALLY_PAID";
            paymentMethod = paymentMethod != null ? paymentMethod : "NONE";
        } else {
            status = "DUE";
            paymentMethod = "NONE";
        }

        if (bill == null) {
            bill = Bill.builder()
                .invoiceNumber(identifierGenerationService.generateBillValue(organization.getId(), 3))
                .invoiceDate(OffsetDateTime.now())
                .encounter(encounter)
                .patient(patient)
                .organization(organization)
                .build();
        }

        bill.setDueDate(request.getDueDate()); // Can be null
        bill.setTotalAmount(totalAmount);
        bill.setDiscountPercentage(discountPercentage);
        bill.setDiscountAmount(discountAmount);
        bill.setNetAmount(netAmount);
        bill.setPaidAmount(initialPaidAmount);
        bill.setDueAmount(dueAmount);
        bill.setStatus(status);
        bill.setPaymentMethod(paymentMethod);
        OffsetDateTime paymentDate = bill.getPaymentDate();
        if (paymentDate == null && status.equals("PAID")) {
            paymentDate = OffsetDateTime.now();
        }
        bill.setPaymentDate(paymentDate);
        bill.setNotes(request.getNotes());

        Bill savedBill = billRepository.save(bill);
        return mapToBillResponse(savedBill);
    }

    /**
     * Records a payment against an existing bill.
     * @param billId The ID of the bill to update.
     * @param request The DTO containing payment details.
     * @return The updated BillResponse.
     */
    @Transactional
    public BillResponse recordPayment(Integer billId, BillPaymentRequest request) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + billId));

        // --- Multi-tenancy check ---
        if (!securityService.isUserInOrganization(bill.getOrganization().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to record payments for organization ID: " + bill.getOrganization().getId());
        }
        // --- End multi-tenancy check ---

        if (bill.getStatus().equals("PAID")) {
            throw new IllegalStateException("Bill is already fully paid.");
        }
        if (bill.getStatus().equals("CANCELLED")) {
            throw new IllegalStateException("Cannot record payment for a cancelled bill.");
        }

        BigDecimal newPaidAmount = bill.getPaidAmount().add(request.getAmountPaid()).setScale(2, RoundingMode.HALF_UP);
        if (newPaidAmount.compareTo(bill.getNetAmount()) > 0) {
            throw new IllegalArgumentException("Amount paid exceeds the outstanding amount. Outstanding: " + bill.getDueAmount());
        }

        bill.setPaidAmount(newPaidAmount);
        bill.setDueAmount(bill.getNetAmount().subtract(newPaidAmount).setScale(2, RoundingMode.HALF_UP));
        bill.setPaymentMethod(request.getPaymentMethod()); // Updates to the last payment method
        bill.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : OffsetDateTime.now());

        if (bill.getDueAmount().compareTo(BigDecimal.ZERO) == 0) {
            bill.setStatus("PAID");
        } else {
            bill.setStatus("PARTIALLY_PAID");
        }

        Bill updatedBill = billRepository.save(bill);
        return mapToBillResponse(updatedBill);
    }

    @Transactional
    public BillResponse syncBillWithEncounter(Integer encounterId) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new RuntimeException("Encounter not found with ID: " + encounterId));
        
        if (!securityService.isUserInOrganization(encounter.getPatient().getOrganization().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized");
        }

        List<Bill> bills = billRepository.findByEncounterOrderByCreatedAtAsc(encounter);
        if (bills.isEmpty()) {
            throw new RuntimeException("No bill found to sync.");
        }
        
        Bill bill = bills.get(0); // sync the first/main bill
        Organization organization = encounter.getPatient().getOrganization();
        
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findByEncounter(encounter);
        BigDecimal newTotalAmount = BigDecimal.ZERO;

        List<ServiceRequestItem> allItems = serviceRequestItemRepository.findByServiceRequestIn(serviceRequests);
        List<Integer> testIds = allItems.stream().map(i -> i.getTest().getId()).distinct().toList();
        Map<Integer, OrganizationTest> orgTestMap = organizationTestRepository.findByOrganization_IdAndTest_IdIn(organization.getId(), testIds)
                .stream().collect(Collectors.toMap(ot -> ot.getTest().getId(), ot -> ot));

        for (ServiceRequest sr : serviceRequests) {
            List<ServiceRequestItem> srItems = allItems.stream()
                    .filter(i -> i.getServiceRequest().getId().equals(sr.getId())).toList();
            for (ServiceRequestItem item : srItems) {
                OrganizationTest orgTest = orgTestMap.get(item.getTest().getId());
                if (orgTest == null) throw new RuntimeException("Test '" + item.getTest().getTestName() + "' not configured");
                newTotalAmount = newTotalAmount.add(orgTest.getPrice());
            }
        }
        
        BigDecimal discountPercentage = bill.getDiscountPercentage() != null ? bill.getDiscountPercentage() : BigDecimal.ZERO;
        BigDecimal discountFactor = discountPercentage.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = newTotalAmount.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newNetAmount = newTotalAmount.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
        
        bill.setTotalAmount(newTotalAmount);
        bill.setDiscountAmount(discountAmount);
        bill.setNetAmount(newNetAmount);
        
        BigDecimal newDueAmount = newNetAmount.subtract(bill.getPaidAmount()).setScale(2, RoundingMode.HALF_UP);
        bill.setDueAmount(newDueAmount);
        
        if (newDueAmount.compareTo(BigDecimal.ZERO) <= 0) {
            bill.setStatus("PAID");
        } else if (bill.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            bill.setStatus("PARTIALLY_PAID");
        } else {
            bill.setStatus("DUE");
        }
        
        Bill savedBill = billRepository.save(bill);
        return mapToBillResponse(savedBill);
    }

    @Transactional(readOnly = true)
    public BillResponse getBillById(Integer id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + id));

        // --- Multi-tenancy check ---
        if (!securityService.isUserInOrganization(bill.getOrganization().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to view bills for organization ID: " + bill.getOrganization().getId());
        }
        // --- End multi-tenancy check ---

        return mapToBillResponse(bill);
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getBillsByEncounter(Integer encounterId) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new RuntimeException("Encounter not found with ID: " + encounterId));

        // --- Multi-tenancy check ---
        if (!securityService.isUserInOrganization(encounter.getPatient().getOrganization().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to view bills for organization ID: " + encounter.getPatient().getOrganization().getId());
        }
        // --- End multi-tenancy check ---

        return billRepository.findByEncounterOrderByCreatedAtAsc(encounter).stream()
                .map(this::mapToBillResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getBillsByOrganization(Integer organizationId, String status) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));

        // --- Multi-tenancy check ---
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to view bills for organization ID: " + organizationId);
        }
        // --- End multi-tenancy check ---

        List<Bill> bills;
        if (status != null && !status.isEmpty()) {
            bills = billRepository.findByOrganization_IdAndStatus(organizationId, status);
        } else {
            bills = billRepository.findByOrganization(organization);
        }

        return bills.stream()
                .map(this::mapToBillResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BillableDetailsResponse getBillableDetailsForEncounter(Integer encounterId) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new RuntimeException("Encounter not found with ID: " + encounterId));

        Organization organization = encounter.getPatient().getOrganization();

        // --- Multi-tenancy check ---
        if (!securityService.isUserInOrganization(organization.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized for organization ID: " + organization.getId());
        }
        // --- End multi-tenancy check ---

        List<ServiceRequest> serviceRequests = serviceRequestRepository.findByEncounter(encounter);
        List<ServiceRequestItem> allItems = serviceRequestItemRepository.findByServiceRequestIn(serviceRequests);
        List<Integer> testIds = allItems.stream().map(i -> i.getTest().getId()).distinct().toList();
        Map<Integer, OrganizationTest> orgTestMap = organizationTestRepository.findByOrganization_IdAndTest_IdIn(organization.getId(), testIds)
                .stream().collect(Collectors.toMap(ot -> ot.getTest().getId(), ot -> ot));

        List<BillableServiceRequest> billableServiceRequests = new ArrayList<>();
        for (ServiceRequest sr : serviceRequests) {
            List<ServiceRequestItem> srItems = allItems.stream()
                    .filter(i -> i.getServiceRequest().getId().equals(sr.getId())).toList();
            List<BillableTest> billableTests = new ArrayList<>();
            for (ServiceRequestItem item : srItems) {
                Test test = item.getTest();
                OrganizationTest orgTest = orgTestMap.get(test.getId());
                BigDecimal price = orgTest != null ? orgTest.getPrice() : null;
                billableTests.add(new BillableTest(test.getId(), test.getTestName(), price));
            }
            billableServiceRequests.add(new BillableServiceRequest(sr.getId(), billableTests));
        }

        return new BillableDetailsResponse(encounter.getLocalEncounterValue(), billableServiceRequests);
    }

    public PagedResponse<BillListResponse> searchBills(
            Integer organizationId, LocalDate startDate, LocalDate endDate, String query, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Specification<Bill> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Bill, Patient> patientJoin = root.join("patient");

            predicates.add(cb.equal(root.get("organization").get("id"), organizationId));

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("invoiceDate"), startDate.atStartOfDay().atOffset(ZoneOffset.UTC)));
            }
            if (endDate != null) {
                predicates.add(cb.lessThan(root.get("invoiceDate"), endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC)));
            }

            if (StringUtils.isNotBlank(query)) {
                String likePattern = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(patientJoin.get("firstName")), likePattern),
                        cb.like(cb.lower(patientJoin.get("lastName")), likePattern),
                        cb.like(cb.lower(patientJoin.get("localMrnValue")), likePattern),
                        cb.like(cb.lower(root.get("invoiceNumber")), likePattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Bill> billPage = billRepository.findAll(spec, pageable);
        List<Bill> bills = billPage.getContent();

        if (bills.isEmpty()) {
            return new PagedResponse<>(Collections.emptyList(), page, size, 0, 0);
        }

        List<Encounter> encounters = bills.stream().map(Bill::getEncounter).distinct().collect(Collectors.toList());
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findByEncounterIn(encounters);
        List<ServiceRequestItem> serviceRequestItems = serviceRequestItemRepository.findByServiceRequestIn(serviceRequests);
        
        // Fetch test prices for this organization to populate TestItems in the response
        List<OrganizationTest> orgTests = organizationTestRepository.findByOrganization_Id(organizationId);
        Map<Integer, BigDecimal> testPriceMap = orgTests.stream()
                .filter(ot -> ot.getTest() != null)
                .collect(Collectors.toMap(
                        ot -> ot.getTest().getId(),
                        ot -> ot.getPrice() != null ? ot.getPrice() : BigDecimal.ZERO,
                        (p1, p2) -> p1
                ));

        Map<Integer, List<ServiceRequest>> encounterToSrMap = serviceRequests.stream()
                .collect(Collectors.groupingBy(sr -> sr.getEncounter().getId()));

        Map<Integer, List<String>> srToTestsMap = new HashMap<>();
        for (ServiceRequestItem item : serviceRequestItems) {
            if (item.getServiceRequest() != null && item.getTest() != null) {
                srToTestsMap
                        .computeIfAbsent(item.getServiceRequest().getId(), k -> new ArrayList<>())
                        .add(item.getTest().getTestName());
            }
        }

        List<BillListResponse> content = bills.stream().map(bill -> {
            Encounter encounter = bill.getEncounter();
            Patient patient = bill.getPatient();
            List<ServiceRequest> relatedSrs = encounterToSrMap.getOrDefault(encounter.getId(), Collections.emptyList());
            List<Integer> relatedSrIds = relatedSrs.stream().map(ServiceRequest::getId).collect(Collectors.toList());
            
            List<BillListResponse.TestItem> testItems = relatedSrs.stream()
                    .flatMap(sr -> serviceRequestItems.stream()
                        .filter(item -> item.getServiceRequest().getId().equals(sr.getId()) && item.getTest() != null)
                        .map(item -> new BillListResponse.TestItem(
                                item.getTest().getTestName(), 
                                testPriceMap.getOrDefault(item.getTest().getId(), BigDecimal.ZERO))))
                    .collect(Collectors.toList());

            List<String> testNames = testItems.stream()
                    .map(BillListResponse.TestItem::getTestName)
                    .distinct()
                    .collect(Collectors.toList());

            return new BillListResponse(
                    bill.getId(),
                    bill.getInvoiceNumber(),
                    bill.getInvoiceDate(),
                    patient.getFirstName() + " " + patient.getLastName(),
                    patient.getLocalMrnValue(),
                    encounter.getLocalEncounterValue(),
                    bill.getTotalAmount(),
                    bill.getDiscountAmount(),
                    bill.getNetAmount(),
                    bill.getPaidAmount(),
                    bill.getDiscountPercentage(),
                    bill.getStatus(),
                    relatedSrIds,
                    testNames,
                    testItems
            );
        }).collect(Collectors.toList());

        PagedResponse<BillListResponse> response = new PagedResponse<>();
        response.setContent(content);
        response.setPage(billPage.getNumber());
        response.setSize(billPage.getSize());
        response.setTotalElements(billPage.getTotalElements());
        response.setTotalPages(billPage.getTotalPages());

        return response;
    }

    private BillResponse mapToBillResponse(Bill bill) {
        BillResponse response = new BillResponse();
        response.setId(bill.getId());
        response.setInvoiceNumber(bill.getInvoiceNumber());
        response.setInvoiceDate(bill.getInvoiceDate());
        response.setDueDate(bill.getDueDate());

        response.setEncounterId(bill.getEncounter().getId());
        response.setEncounterLocalValue(bill.getEncounter().getLocalEncounterValue());

        response.setPatientId(bill.getPatient().getId());
        response.setPatientMrn(bill.getPatient().getLocalMrnValue());
        response.setPatientName(bill.getPatient().getFirstName() + " " + bill.getPatient().getLastName());
        // Decrypt contact phone for response
        response.setPatientContactPhone(aesGcmEncryptionUtil.decrypt(bill.getPatient().getContactPhone()));

        response.setOrganizationId(bill.getOrganization().getId());
        response.setOrganizationName(bill.getOrganization().getOrganizationName());

        response.setTotalAmount(bill.getTotalAmount());
        response.setDiscountPercentage(bill.getDiscountPercentage());
        response.setDiscountAmount(bill.getDiscountAmount());
        response.setNetAmount(bill.getNetAmount());
        response.setPaidAmount(bill.getPaidAmount());
        response.setDueAmount(bill.getDueAmount());

        response.setStatus(bill.getStatus());
        response.setPaymentMethod(bill.getPaymentMethod());
        response.setPaymentDate(bill.getPaymentDate());
        response.setNotes(bill.getNotes());
        response.setCreatedAt(bill.getCreatedAt());
        response.setUpdatedAt(bill.getUpdatedAt());

        // Map covered Service Requests
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findByEncounterIn(Collections.singletonList(bill.getEncounter()));
        List<ServiceRequestItem> items = serviceRequestItemRepository.findByServiceRequestIn(serviceRequests);
        
        // Fetch prices
        List<OrganizationTest> orgTests = organizationTestRepository.findByOrganization_Id(bill.getOrganization().getId());
        Map<Integer, BigDecimal> priceMap = orgTests.stream()
                .filter(ot -> ot.getTest() != null)
                .collect(Collectors.toMap(ot -> ot.getTest().getId(), OrganizationTest::getPrice, (p1, p2) -> p1));

        List<BillResponse.BillServiceRequestDetails> srDetails = serviceRequests.stream().map(sr -> {
            BillResponse.BillServiceRequestDetails details = new BillResponse.BillServiceRequestDetails();
            details.setServiceRequestId(sr.getId());
            details.setServiceRequestLocalValue(sr.getLocalOrderValue());
            details.setStatus(sr.getStatus());
            details.setPriority(sr.getPriority());
            
            List<ServiceRequestResponse.TestDetailsResponse> tests = items.stream()
                    .filter(item -> item.getServiceRequest().getId().equals(sr.getId()) && item.getTest() != null)
                    .map(item -> {
                        ServiceRequestResponse.TestDetailsResponse t = new ServiceRequestResponse.TestDetailsResponse();
                        t.setTestId(item.getTest().getId());
                        t.setTestName(item.getTest().getTestName());
                        t.setTestLocalCode(item.getTest().getLocalCode());
                        t.setStatus(item.getStatus());
                        t.setPrice(priceMap.getOrDefault(item.getTest().getId(), BigDecimal.ZERO));
                        return t;
                    }).collect(Collectors.toList());
            
            details.setRequestedTests(tests);
            return details;
        }).collect(Collectors.toList());

        response.setServiceRequests(srDetails);
        return response;
    }
}

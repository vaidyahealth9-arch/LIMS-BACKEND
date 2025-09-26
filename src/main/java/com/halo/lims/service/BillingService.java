package com.halo.lims.service;

import com.halo.lims.dto.billing.BillCreateRequest;
import com.halo.lims.dto.billing.BillPaymentRequest;
import com.halo.lims.dto.billing.BillResponse;
import com.halo.lims.model.*;
import com.halo.lims.repository.*;
import com.halo.lims.security.AesGcmEncryptionUtil;
import com.halo.lims.security.SecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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

    public BillingService(BillRepository billRepository,
                          EncounterRepository encounterRepository,
                          ServiceRequestRepository serviceRequestRepository,
                          ServiceRequestItemRepository serviceRequestItemRepository,
                          OrganizationTestRepository organizationTestRepository, OrganizationRepository organizationRepository,
                          SecurityService securityService,
                          AesGcmEncryptionUtil aesGcmEncryptionUtil) {
        this.billRepository = billRepository;
        this.encounterRepository = encounterRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.serviceRequestItemRepository = serviceRequestItemRepository;
        this.organizationTestRepository = organizationTestRepository;
        this.organizationRepository = organizationRepository;
        this.securityService = securityService;
        this.aesGcmEncryptionUtil = aesGcmEncryptionUtil;
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

        List<ServiceRequest> serviceRequests = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Integer srId : request.getServiceRequestIds()) {
            ServiceRequest sr = serviceRequestRepository.findById(srId)
                    .orElseThrow(() -> new RuntimeException("Service Request with ID " + srId + " not found."));
            if (!sr.getPatient().getId().equals(patient.getId())) {
                throw new IllegalArgumentException("Service Request " + srId + " does not belong to patient " + patient.getId());
            }
            if (!sr.getEncounter().getId().equals(encounter.getId())) {
                throw new IllegalArgumentException("Service Request " + srId + " does not belong to encounter " + encounter.getId());
            }

            serviceRequests.add(sr);

            // Calculate total amount from service request items
            List<ServiceRequestItem> srItems = serviceRequestItemRepository.findByServiceRequest(sr);
            for (ServiceRequestItem item : srItems) {
                // Fetch price from organization_tests
                OrganizationTest orgTest = organizationTestRepository.findByOrganization_IdAndTest_Id(organization.getId(), item.getTest().getId())
                        .orElseThrow(() -> new RuntimeException("Test '" + item.getTest().getTestName() + "' is not configured for organization " + organization.getOrganizationName()));
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

        BigDecimal initialPaidAmount = request.getInitialPaidAmount() != null ? request.getInitialPaidAmount() : BigDecimal.ZERO;
        if (initialPaidAmount.compareTo(netAmount) > 0) {
            throw new IllegalArgumentException("Initial paid amount cannot exceed net amount.");
        }
        BigDecimal dueAmount = netAmount.subtract(initialPaidAmount).setScale(2, RoundingMode.HALF_UP);

        String status;
        String paymentMethod = request.getInitialPaymentMethod();
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

        Bill bill = Bill.builder()
                .invoiceNumber(generateInvoiceNumber(organization.getLocalIdentifierValue()))
                .invoiceDate(OffsetDateTime.now())
                .dueDate(request.getDueDate()) // Can be null
                .encounter(encounter)
                .patient(patient)
                .organization(organization)
                .totalAmount(totalAmount)
                .discountPercentage(discountPercentage)
                .discountAmount(discountAmount)
                .netAmount(netAmount)
                .paidAmount(initialPaidAmount)
                .dueAmount(dueAmount)
                .status(status)
                .paymentMethod(paymentMethod)
                .paymentDate(status.equals("PAID") ? OffsetDateTime.now() : null)
                .notes(request.getNotes())
                .build();

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

        return billRepository.findByEncounter(encounter).stream()
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

    private String generateInvoiceNumber(String organizationLocalId) {
        // Example: INV-ORG_CODE-YYMMDD-XXXX
        String datePart = OffsetDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        // This counter needs to be robust (e.g., sequence per organization) in production
        String suffix = String.format("%04d", (billRepository.count() + 1));
        return "INV-" + organizationLocalId + "-" + datePart + "-" + suffix;
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

        // Map covered Service Requests (fetching details via ServiceRequestService is better here)
        List<BillResponse.BillServiceRequestDetails> srDetails = new ArrayList<>();
        // This part needs to be retrieved more robustly, maybe passing serviceRequestRepository directly
        // For now, let's assume you fetch ServiceRequests covered by this bill via other means if needed.
        // Or refactor to take a List<ServiceRequest> in the constructor to map.
        // For simplicity, we'll return an empty list or a hardcoded stub here.
        response.setServiceRequests(srDetails); // Placeholder: You'll need to fill this in with actual SR details

        response.setCreatedAt(bill.getCreatedAt());
        response.setUpdatedAt(bill.getUpdatedAt());
        return response;
    }
}

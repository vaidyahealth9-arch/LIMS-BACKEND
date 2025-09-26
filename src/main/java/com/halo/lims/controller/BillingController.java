package com.halo.lims.controller;

import com.halo.lims.dto.billing.BillCreateRequest;
import com.halo.lims.dto.billing.BillPaymentRequest;
import com.halo.lims.dto.billing.BillResponse;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillingController {

    private final BillingService billingService;
    private final SecurityService securityService; // Needed for @PreAuthorize checks

    public BillingController(BillingService billingService, SecurityService securityService) {
        this.billingService = billingService;
        this.securityService = securityService;
    }

    /**
     * Creates a new bill for a patient encounter.
     * Accessible by RECEPTIONIST, MANAGER, ADMIN roles.
     * @param request DTO for creating a bill.
     * @return Created BillResponse.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST') and @securityService.canAccessEncounter(#request.encounterId)")
    public ResponseEntity<BillResponse> createBill(@Valid @RequestBody BillCreateRequest request) {
        BillResponse response = billingService.createBill(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Records a payment against an existing bill.
     * Accessible by RECEPTIONIST, MANAGER, ADMIN roles.
     * @param id The ID of the bill.
     * @param request DTO for recording a payment.
     * @return Updated BillResponse.
     */
    @PatchMapping("/{id}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST') and @securityService.canAccessBill(#id)")
    public ResponseEntity<BillResponse> recordPayment(@PathVariable Integer id, @Valid @RequestBody BillPaymentRequest request) {
        BillResponse response = billingService.recordPayment(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves a single bill by its ID.
     * Accessible by various roles.
     * @param id The ID of the bill.
     * @return BillResponse.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST', 'DOCTOR') and @securityService.canAccessBill(#id)")
    public ResponseEntity<BillResponse> getBillById(@PathVariable Integer id) {
        BillResponse response = billingService.getBillById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves all bills for a given encounter.
     * Accessible by various roles.
     * @param encounterId The ID of the encounter.
     * @return List of BillResponses.
     */
    @GetMapping("/by-encounter/{encounterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST', 'DOCTOR') and @securityService.canAccessEncounter(#encounterId)")
    public ResponseEntity<List<BillResponse>> getBillsByEncounter(@PathVariable Integer encounterId) {
        List<BillResponse> responses = billingService.getBillsByEncounter(encounterId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Retrieves all bills for a given organization, optionally filtered by status.
     * Accessible by ADMIN or MANAGER for their organization.
     * @param organizationId The ID of the organization.
     * @param status Optional: filter by bill status (e.g., "DUE", "PAID").
     * @return List of BillResponses.
     */
    @GetMapping("/by-organization/{organizationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') and @securityService.isUserInOrganization(#organizationId)")
    public ResponseEntity<List<BillResponse>> getBillsByOrganization(
            @PathVariable Integer organizationId,
            @RequestParam(required = false) String status) {
        List<BillResponse> responses = billingService.getBillsByOrganization(organizationId, status);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}

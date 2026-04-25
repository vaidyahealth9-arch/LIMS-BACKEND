package com.halo.lims.service;

import com.halo.lims.constant.EncounterStatus;
import com.halo.lims.dto.encounter.EncounterResponse;
import com.halo.lims.dto.report.ReportApprovalStatusResponse;
import com.halo.lims.model.Bill;
import com.halo.lims.model.Encounter;
import com.halo.lims.model.Organization;
import com.halo.lims.model.Patient;
import com.halo.lims.model.Practitioner;
import com.halo.lims.model.ServiceRequest;
import com.halo.lims.repository.BillRepository;
import com.halo.lims.repository.EncounterRepository;
import com.halo.lims.repository.OrganizationRepository;
import com.halo.lims.repository.PatientRepository;
import com.halo.lims.repository.ServiceRequestItemRepository;
import com.halo.lims.repository.ServiceRequestRepository;
import com.halo.lims.repository.SpecimenRepository;
import com.halo.lims.security.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EncounterServiceTest {

    @InjectMocks
    private EncounterService encounterService;

    @Mock private EncounterRepository encounterRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private SecurityService securityService;
    @Mock private ServiceRequestRepository serviceRequestRepository;
    @Mock private ServiceRequestItemRepository serviceRequestItemRepository;
    @Mock private SpecimenRepository specimenRepository;
    @Mock private BillRepository billRepository;
    @Mock private ReportApprovalService reportApprovalService;
    @Mock private ReportService reportService;
    @Mock private IdentifierGenerationService identifierGenerationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateEncounterWorkflowStatus_shouldRejectManualApprovedTransition() {
        Encounter encounter = buildEncounter(EncounterStatus.PENDING_VERIFICATION.getCode());

        when(encounterRepository.findById(30)).thenReturn(Optional.of(encounter));
        when(securityService.isUserInOrganization(10)).thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> encounterService.updateEncounterWorkflowStatus(30, "APPROVED", Practitioner.builder().id(90).build())
        );

        assertEquals(
                "Doctor approval must be completed from the observation approval workflow. Encounter approval is assigned automatically after results are finalized.",
                ex.getMessage()
        );
    }

    @Test
    void updateEncounterWorkflowStatus_shouldRejectCompletedWhenBillHasDueAmount() {
        Encounter encounter = buildEncounter(EncounterStatus.APPROVED.getCode());

        when(encounterRepository.findById(30)).thenReturn(Optional.of(encounter));
        when(securityService.isUserInOrganization(10)).thenReturn(true);
        when(billRepository.findByEncounter(encounter)).thenReturn(List.of(
                Bill.builder()
                        .id(77)
                        .netAmount(new BigDecimal("600.00"))
                        .paidAmount(new BigDecimal("300.00"))
                        .dueAmount(new BigDecimal("300.00"))
                        .status("PARTIALLY_PAID")
                        .build()
        ));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> encounterService.updateEncounterWorkflowStatus(30, "COMPLETED", null)
        );

        assertEquals("Cannot complete encounter. Full payment is still pending.", ex.getMessage());
    }

    @Test
    void updateEncounterWorkflowStatus_shouldCompleteWhenBillsSettledAndReportsDownloadable() {
        Encounter encounter = buildEncounter(EncounterStatus.APPROVED.getCode());
        ServiceRequest serviceRequest = ServiceRequest.builder()
                .id(40)
                .patient(encounter.getPatient())
                .encounter(encounter)
                .localOrderValue("ORD-040")
                .orderDate(OffsetDateTime.now().minusHours(1))
                .build();

        when(encounterRepository.findById(30)).thenReturn(Optional.of(encounter));
        when(securityService.isUserInOrganization(10)).thenReturn(true);
        when(billRepository.findByEncounter(encounter)).thenReturn(List.of(
                Bill.builder()
                        .id(77)
                        .netAmount(new BigDecimal("600.00"))
                        .paidAmount(new BigDecimal("600.00"))
                        .dueAmount(BigDecimal.ZERO)
                        .status("PAID")
                        .build()
        ));
        when(serviceRequestRepository.findByEncounter(encounter)).thenReturn(List.of(serviceRequest));
        when(reportApprovalService.getReportApprovalStatus(40)).thenReturn(
                ReportApprovalStatusResponse.builder()
                        .ready(true)
                        .message("Approved and ready for report generation.")
                        .build()
        );
        when(reportService.buildUnifiedPdfReport(40, true, "regular")).thenReturn(new byte[0]);
        when(encounterRepository.save(any(Encounter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EncounterResponse response = encounterService.updateEncounterWorkflowStatus(30, "COMPLETED", null);

        assertEquals("COMPLETED", response.getStatus());
        verify(reportService).buildUnifiedPdfReport(40, true, "regular");
        verify(encounterRepository).save(eq(encounter));
    }

    private Encounter buildEncounter(String status) {
        Organization organization = Organization.builder()
                .id(10)
                .organizationName("Demo Lab")
                .localIdentifierSystem("http://local/org")
                .localIdentifierValue("ORG001")
                .orgType("laboratory")
                .build();

        Patient patient = Patient.builder()
                .id(20)
                .firstName("Pooja")
                .lastName("K")
                .gender("female")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .organization(organization)
                .localMrnSystem("http://local/mrn")
                .localMrnValue("MRN001")
                .build();

        return Encounter.builder()
                .id(30)
                .patient(patient)
                .serviceProvider(organization)
                .status(status)
                .startTime(OffsetDateTime.now().minusHours(2))
                .localEncounterSystem("http://local/encounter")
                .localEncounterValue("ENC-030")
                .build();
    }
}

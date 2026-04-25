package com.halo.lims.service;

import com.halo.lims.dto.report.ReportApprovalStatusResponse;
import com.halo.lims.model.Observation;
import com.halo.lims.model.Encounter;
import com.halo.lims.model.Organization;
import com.halo.lims.model.Patient;
import com.halo.lims.model.Practitioner;
import com.halo.lims.model.ServiceRequest;
import com.halo.lims.model.User;
import com.halo.lims.repository.DiagnosticReportRepository;
import com.halo.lims.repository.ObservationRepository;
import com.halo.lims.repository.ServiceRequestRepository;
import com.halo.lims.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    private ReportApprovalService reportApprovalService;

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private ObservationRepository observationRepository;

    @Mock
    private DiagnosticReportRepository diagnosticReportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HashidService hashidService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reportApprovalService = new ReportApprovalService(
                serviceRequestRepository,
                observationRepository,
                diagnosticReportRepository,
                userRepository,
                null,
                hashidService);
    }

    @Test
    void getReportApprovalStatus_acceptsRolePrefixedDoctorRole() {
        Organization organization = Organization.builder()
                .id(10)
                .organizationName("Demo Org")
                .orgType("laboratory")
                .localIdentifierSystem("http://local/org")
                .localIdentifierValue("ORG001")
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

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .id(30)
                .patient(patient)
                .localOrderSystem("http://local/order")
                .localOrderValue("ORD-001")
                .orderDate(OffsetDateTime.now().minusHours(2))
                .build();

        Practitioner approvingDoctor = Practitioner.builder()
                .id(40)
                .firstName("Asha")
                .lastName("Doctor")
                .localIdentifierSystem("http://local/pr")
                .localIdentifierValue("PR-001")
                .build();

        Observation finalObservation = Observation.builder()
                .id(50)
                .serviceRequest(serviceRequest)
                .patient(patient)
                .status("final")
                .issuedDateTime(OffsetDateTime.now())
                .effectiveDateTime(OffsetDateTime.now().minusMinutes(30))
                .performer(approvingDoctor)
                .localObservationSystem("http://local/obs")
                .localObservationValue("OBS-001")
                .build();

        User approvingUser = User.builder()
                .id(60)
                .username("doctor.user")
                .password("irrelevant")
                .roles(Set.of("ROLE_DOCTOR"))
                .practitioner(approvingDoctor)
                .organization(organization)
                .isActive(true)
                .build();

        when(serviceRequestRepository.findById(30)).thenReturn(Optional.of(serviceRequest));
        when(observationRepository.findByServiceRequestId(30)).thenReturn(List.of(finalObservation));
        when(diagnosticReportRepository.findByServiceRequest_Id(30)).thenReturn(Optional.empty());
        when(userRepository.findByPractitioner_Id(40)).thenReturn(Optional.of(approvingUser));
        ReportApprovalStatusResponse response = reportApprovalService.getReportApprovalStatus(30);

        assertTrue(response.isReady());
        assertEquals("Approved and ready for report generation.", response.getMessage());
        assertEquals("Asha Doctor", response.getApprovedDoctorName());
    }

    @Test
    void getReportApprovalStatus_doesNotTreatEncounterStatusAloneAsApproval() {
        Organization organization = Organization.builder()
                .id(10)
                .organizationName("Demo Org")
                .orgType("laboratory")
                .localIdentifierSystem("http://local/org")
                .localIdentifierValue("ORG001")
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

        Encounter encounter = Encounter.builder()
                .id(25)
                .patient(patient)
                .serviceProvider(organization)
                .status("approved")
                .startTime(OffsetDateTime.now().minusHours(3))
                .localEncounterSystem("http://local/encounter")
                .localEncounterValue("ENC-025")
                .build();

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .id(30)
                .patient(patient)
                .encounter(encounter)
                .localOrderSystem("http://local/order")
                .localOrderValue("ORD-001")
                .orderDate(OffsetDateTime.now().minusHours(2))
                .build();

        Practitioner approvingDoctor = Practitioner.builder()
                .id(40)
                .firstName("Asha")
                .lastName("Doctor")
                .localIdentifierSystem("http://local/pr")
                .localIdentifierValue("PR-001")
                .build();

        Observation nonFinalObservation = Observation.builder()
                .id(50)
                .serviceRequest(serviceRequest)
                .patient(patient)
                .status("preliminary")
                .issuedDateTime(OffsetDateTime.now())
                .effectiveDateTime(OffsetDateTime.now().minusMinutes(30))
                .performer(approvingDoctor)
                .localObservationSystem("http://local/obs")
                .localObservationValue("OBS-001")
                .build();

        when(serviceRequestRepository.findById(30)).thenReturn(Optional.of(serviceRequest));
        when(observationRepository.findByServiceRequestId(30)).thenReturn(List.of(nonFinalObservation));
        when(diagnosticReportRepository.findByServiceRequest_Id(30)).thenReturn(Optional.empty());

        ReportApprovalStatusResponse response = reportApprovalService.getReportApprovalStatus(30);

        assertEquals(false, response.isReady());
        assertEquals("All results must be doctor-verified and final before report generation.", response.getMessage());
    }
}

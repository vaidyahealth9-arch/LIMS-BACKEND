package com.halo.lims.service;

import com.halo.lims.dto.observation.ObservationResponse;
import com.halo.lims.model.Encounter;
import com.halo.lims.model.Observation;
import com.halo.lims.model.Organization;
import com.halo.lims.model.Patient;
import com.halo.lims.model.Practitioner;
import com.halo.lims.model.ServiceRequest;
import com.halo.lims.model.TestAnalyte;
import com.halo.lims.repository.DiagnosticReportObservationRepository;
import com.halo.lims.repository.DiagnosticReportRepository;
import com.halo.lims.repository.EncounterRepository;
import com.halo.lims.repository.ObservationRepository;
import com.halo.lims.repository.OrganizationAnalyteInterpretationRuleRepository;
import com.halo.lims.repository.PractitionerRepository;
import com.halo.lims.repository.ReferenceRangeRepository;
import com.halo.lims.repository.ServiceRequestRepository;
import com.halo.lims.repository.SpecimenRepository;
import com.halo.lims.repository.TestAnalyteRepository;
import com.halo.lims.repository.TestInterpretationRuleRepository;
import com.halo.lims.security.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ObservationServiceTest {

    @InjectMocks
    private ObservationService observationService;

    @Mock private ObservationRepository observationRepository;
    @Mock private OrganizationAnalyteInterpretationRuleRepository organizationAnalyteInterpretationRuleRepository;
    @Mock private ServiceRequestRepository serviceRequestRepository;
    @Mock private EncounterRepository encounterRepository;
    @Mock private SpecimenRepository specimenRepository;
    @Mock private TestAnalyteRepository testAnalyteRepository;
    @Mock private ReferenceRangeRepository referenceRangeRepository;
    @Mock private TestInterpretationRuleRepository testInterpretationRuleRepository;
    @Mock private DiagnosticReportRepository diagnosticReportRepository;
    @Mock private DiagnosticReportObservationRepository diagnosticReportObservationRepository;
    @Mock private PractitionerRepository practitionerRepository;
    @Mock private ReportService reportService;
    @Mock private ReportStorageService reportStorageService;
    @Mock private SecurityService securityService;
    @Mock private IdentifierGenerationService identifierGenerationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void approveObservations_shouldNotFailWhenReportGenerationFails() {
        Organization organization = Organization.builder()
                .id(10)
                .organizationName("Demo Lab")
                .orgType("laboratory")
                .localIdentifierSystem("http://local/org")
                .localIdentifierValue("ORG001")
                .build();

        Patient patient = Patient.builder()
                .id(20)
                .firstName("Pooja")
                .gender("female")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .organization(organization)
                .localMrnSystem("http://local/mrn")
                .localMrnValue("MRN001")
                .build();

        Encounter encounter = Encounter.builder().id(30).patient(patient).serviceProvider(organization).build();

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .id(40)
                .patient(patient)
                .encounter(encounter)
                .localOrderSystem("http://local/order")
                .localOrderValue("ORD-001")
                .orderDate(OffsetDateTime.now().minusHours(2))
                .build();

        Practitioner pathologist = Practitioner.builder()
                .id(50)
                .firstName("Asha")
                .lastName("Doctor")
                .localIdentifierSystem("http://local/pr")
                .localIdentifierValue("PR-001")
                .build();

        com.halo.lims.model.Test test = com.halo.lims.model.Test.builder()
                .id(70)
                .testName("CBC")
                .localCode("T-70")
                .build();

        TestAnalyte analyte = TestAnalyte.builder()
                .id(80)
                .analyteName("Hemoglobin")
                .analyteCode("HB")
                .parentTest(test)
                .resultType("numeric")
                .isDerived(false)
                .build();

        Observation pendingObservation = Observation.builder()
                .id(60)
                .serviceRequest(serviceRequest)
                .patient(patient)
                .status("pending-verification")
                .issuedDateTime(OffsetDateTime.now().minusMinutes(10))
                .effectiveDateTime(OffsetDateTime.now().minusMinutes(20))
                .performer(pathologist)
                .analyte(analyte)
                .localObservationSystem("http://local/obs")
                .localObservationValue("OBS-001")
                .build();

        Observation finalizedObservation = Observation.builder()
                .id(60)
                .serviceRequest(serviceRequest)
                .patient(patient)
                .status("final")
                .issuedDateTime(OffsetDateTime.now())
                .effectiveDateTime(OffsetDateTime.now().minusMinutes(20))
                .performer(pathologist)
                .analyte(analyte)
                .localObservationSystem("http://local/obs")
                .localObservationValue("OBS-001")
                .build();

        when(observationRepository.findAllById(List.of(60))).thenReturn(List.of(pendingObservation));
        when(securityService.isUserInOrganization(10)).thenReturn(true);
        when(practitionerRepository.findById(50)).thenReturn(Optional.of(pathologist));
        when(observationRepository.saveAll(any())).thenReturn(List.of(finalizedObservation));
        when(observationRepository.findByServiceRequestId(40)).thenReturn(List.of(finalizedObservation));
        when(serviceRequestRepository.findById(40)).thenReturn(Optional.of(serviceRequest));
        when(serviceRequestRepository.findByEncounter(encounter)).thenReturn(List.of(serviceRequest));
        when(encounterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(diagnosticReportRepository.findByServiceRequest_Id(40)).thenReturn(Optional.empty());
        when(reportService.getStoredOrGeneratedPdfReport(anyInt(), any(Boolean.class), any())).thenThrow(new RuntimeException("storage unavailable"));

                List<ObservationResponse> responses = assertDoesNotThrow(() -> observationService.approveObservations(List.of(60), 50));

                verify(observationRepository).saveAll(any());
        assertEquals(1, responses.size());
    }
}

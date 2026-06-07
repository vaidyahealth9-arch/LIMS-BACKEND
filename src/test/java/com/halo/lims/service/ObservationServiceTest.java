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

    @Test
    void testReferenceRangeInterpretations() {
        // Mock dependencies
        com.halo.lims.model.Organization org = com.halo.lims.model.Organization.builder().id(1).build();
        Patient femalePatient = Patient.builder()
                .id(1)
                .gender("female")
                .dateOfBirth(LocalDate.now().minusYears(30)) // 30 years old
                .organization(org)
                .build();
        
        ServiceRequest sr = ServiceRequest.builder().id(1).patient(femalePatient).build();
        
        com.halo.lims.model.Test test = com.halo.lims.model.Test.builder().id(1).testName("General").build();
        TestAnalyte analyte = TestAnalyte.builder()
                .id(1)
                .analyteCode("ANALYTE")
                .analyteName("Analyte")
                .parentTest(test)
                .resultType("numeric")
                .build();
        
        Practitioner performer = Practitioner.builder().id(1).build();
        
        when(serviceRequestRepository.findById(1)).thenReturn(Optional.of(sr));
        when(testAnalyteRepository.findById(1)).thenReturn(Optional.of(analyte));
        when(practitionerRepository.findById(1)).thenReturn(Optional.of(performer));
        when(observationRepository.save(any(Observation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Case 1: Titer interpretation
        com.halo.lims.model.ReferenceRange rrTiter = com.halo.lims.model.ReferenceRange.builder()
                .id(1)
                .analyte(analyte)
                .textRange("Significant: ≥1:80")
                .build();
        when(referenceRangeRepository.findByAnalyteId(1)).thenReturn(List.of(rrTiter));
        
        // Value: 1:40 (should be N)
        com.halo.lims.dto.observation.ObservationCreateRequest req1 = new com.halo.lims.dto.observation.ObservationCreateRequest();
        req1.setServiceRequestId(1);
        req1.setAnalyteId(1);
        req1.setValueString("1:40");
        ObservationResponse res1 = observationService.createObservation(req1, 1);
        assertEquals("N", res1.getInterpretation());
        
        // Value: 1:160 (should be H)
        com.halo.lims.dto.observation.ObservationCreateRequest req2 = new com.halo.lims.dto.observation.ObservationCreateRequest();
        req2.setServiceRequestId(1);
        req2.setAnalyteId(1);
        req2.setValueString("1:160");
        ObservationResponse res2 = observationService.createObservation(req2, 1);
        assertEquals("H", res2.getInterpretation());

        // Case 2: Multi-range with negative/indeterminate/positive
        com.halo.lims.model.ReferenceRange rrMulti = com.halo.lims.model.ReferenceRange.builder()
                .id(2)
                .analyte(analyte)
                .textRange("<14: Negative | 14-19: Indeterminate | >19: Positive")
                .build();
        when(referenceRangeRepository.findByAnalyteId(1)).thenReturn(List.of(rrMulti));
        
        // Value: 5 (should be N)
        com.halo.lims.dto.observation.ObservationCreateRequest req3 = new com.halo.lims.dto.observation.ObservationCreateRequest();
        req3.setServiceRequestId(1);
        req3.setAnalyteId(1);
        req3.setValueNumeric(new java.math.BigDecimal("5"));
        ObservationResponse res3 = observationService.createObservation(req3, 1);
        assertEquals("N", res3.getInterpretation());
        
        // Value: 15 (should be H for Indeterminate)
        req3.setValueNumeric(new java.math.BigDecimal("15"));
        ObservationResponse res3b = observationService.createObservation(req3, 1);
        assertEquals("H", res3b.getInterpretation());

        // Case 3: Demographic filtering (Female <50yr:15-40, Female >50yr:21-43, Male <50yr:19-44)
        com.halo.lims.model.ReferenceRange rrDemog = com.halo.lims.model.ReferenceRange.builder()
                .id(3)
                .analyte(analyte)
                .textRange("Female <50yr:15-40, Female >50yr:21-43, Male <50yr:19-44")
                .build();
        when(referenceRangeRepository.findByAnalyteId(1)).thenReturn(List.of(rrDemog));
        
        // Patient is female, 30 years old. Normal range should be 15-40.
        // Value: 45 (should be H)
        com.halo.lims.dto.observation.ObservationCreateRequest req4 = new com.halo.lims.dto.observation.ObservationCreateRequest();
        req4.setServiceRequestId(1);
        req4.setAnalyteId(1);
        req4.setValueNumeric(new java.math.BigDecimal("45"));
        ObservationResponse res4 = observationService.createObservation(req4, 1);
        assertEquals("H", res4.getInterpretation());
        
        // Value: 10 (should be L)
        req4.setValueNumeric(new java.math.BigDecimal("10"));
        ObservationResponse res4b = observationService.createObservation(req4, 1);
        assertEquals("L", res4b.getInterpretation());

        // Case 4: Deficiency / Toxicity: Deficiency: <20 | Sufficiency: 30-100 | Toxicity: >100
        com.halo.lims.model.ReferenceRange rrDef = com.halo.lims.model.ReferenceRange.builder()
                .id(4)
                .analyte(analyte)
                .textRange("Deficiency: <20 | Sufficiency: 30-100 | Toxicity: >100")
                .build();
        when(referenceRangeRepository.findByAnalyteId(1)).thenReturn(List.of(rrDef));
        
        // Value: 25 (should be L)
        com.halo.lims.dto.observation.ObservationCreateRequest req5 = new com.halo.lims.dto.observation.ObservationCreateRequest();
        req5.setServiceRequestId(1);
        req5.setAnalyteId(1);
        req5.setValueNumeric(new java.math.BigDecimal("25"));
        ObservationResponse res5 = observationService.createObservation(req5, 1);
        assertEquals("L", res5.getInterpretation());
        
        // Value: 120 (should be H)
        req5.setValueNumeric(new java.math.BigDecimal("120"));
        ObservationResponse res5b = observationService.createObservation(req5, 1);
        assertEquals("H", res5b.getInterpretation());
    }
}

package com.halo.lims.service;

import com.halo.lims.dto.report.DiagnosticReportDTO;
import com.halo.lims.dto.report.ReportApprovalStatusResponse;
import com.halo.lims.model.Organization;
import com.halo.lims.model.Patient;
import com.halo.lims.model.ServiceRequest;
import com.halo.lims.repository.DiagnosticReportRepository;
import com.halo.lims.repository.ObservationRepository;
import com.halo.lims.repository.ServiceRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportServiceStorageFallbackTest {

    @InjectMocks
    private ReportService reportService;

    @Mock private ServiceRequestRepository serviceRequestRepository;
    @Mock private DiagnosticReportRepository diagnosticReportRepository;
    @Mock private ObservationRepository observationRepository;
    @Mock private ReportApprovalService reportApprovalService;
    @Mock private ReportDtoBuilder reportDtoBuilder;
    @Mock private ReportRenderer reportRenderer;
    @Mock private ReportCacheService reportCacheService;
    @Mock private ReportStorageService reportStorageService;
    @Mock private IdentifierGenerationService identifierGenerationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getStoredOrGeneratedPdfReport_shouldReturnFreshPdfWhenStorageFails() {
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

        byte[] expectedPdf = "pdf".getBytes();

        when(serviceRequestRepository.findById(30)).thenReturn(Optional.of(serviceRequest));
        when(reportApprovalService.getReportApprovalStatus(30)).thenReturn(
                ReportApprovalStatusResponse.builder()
                        .ready(true)
                        .message("Approved and ready for report generation.")
                        .build()
        );
        when(diagnosticReportRepository.findByServiceRequest_Id(30)).thenReturn(Optional.empty());
        when(observationRepository.findMaxUpdatedAtByServiceRequestId(30)).thenReturn(null);
        when(reportDtoBuilder.buildReportDTO(eq(30), eq(true), eq("regular"), any(ReportApprovalStatusResponse.class)))
                .thenReturn(DiagnosticReportDTO.builder().reportType("regular").withHeader(true).build());
        when(reportRenderer.renderReportPdf(any(DiagnosticReportDTO.class))).thenReturn(expectedPdf);
        when(identifierGenerationService.generateReportValue(10, 3)).thenReturn("REP001");
        when(reportStorageService.uploadFile(anyString(), any(byte[].class), anyString()))
                .thenThrow(new IllegalStateException("storage unavailable"));

        byte[] result = reportService.getStoredOrGeneratedPdfReport(30, true, "regular");

        assertArrayEquals(expectedPdf, result);
        verify(reportStorageService).uploadFile(anyString(), any(byte[].class), anyString());
        verify(diagnosticReportRepository, never()).save(any());
    }
}

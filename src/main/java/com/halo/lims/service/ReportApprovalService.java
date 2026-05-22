package com.halo.lims.service;

import com.halo.lims.dto.report.ReportApprovalStatusResponse;
import com.halo.lims.model.DiagnosticReport;
import com.halo.lims.model.Organization;
import com.halo.lims.model.Patient;
import com.halo.lims.model.Practitioner;
import com.halo.lims.model.ServiceRequest;
import com.halo.lims.model.Observation;
import com.halo.lims.model.User;
import com.halo.lims.repository.DiagnosticReportRepository;
import com.halo.lims.repository.ObservationRepository;
import com.halo.lims.repository.ServiceRequestRepository;
import com.halo.lims.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class ReportApprovalService {

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.ENGLISH);

    private final ServiceRequestRepository serviceRequestRepository;
    private final ObservationRepository observationRepository;
    private final DiagnosticReportRepository diagnosticReportRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final HashidService hashidService;

    public ReportApprovalService(
            ServiceRequestRepository serviceRequestRepository,
            ObservationRepository observationRepository,
            DiagnosticReportRepository diagnosticReportRepository,
            UserRepository userRepository,
            ImageService imageService,
            HashidService hashidService) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.observationRepository = observationRepository;
        this.diagnosticReportRepository = diagnosticReportRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.hashidService = hashidService;
    }

    @Transactional(readOnly = true)
    public ReportApprovalStatusResponse getReportApprovalStatus(Integer serviceRequestId) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new RuntimeException("Service Request not found with ID: " + serviceRequestId));
        
        com.halo.lims.model.Encounter encounter = serviceRequest.getEncounter();
        ReportMetadata reportMetadata = buildReportMetadata(serviceRequest, null);

        List<Observation> observations = observationRepository.findByServiceRequestId(serviceRequestId);
        if (observations.isEmpty()) {
            return buildNotReadyStatus(
                    String.format("No results found for Order [%s]. Please enter and approve results before generating report.", 
                            serviceRequest.getLocalOrderValue()),
                    reportMetadata);
        }

        // Standard logic for final observations
        boolean allFinal = observations.stream().allMatch(obs -> "final".equalsIgnoreCase(obs.getStatus()));
        if (!allFinal) {
            return buildNotReadyStatus(
                    "All results must be doctor-verified and final before report generation.",
                    reportMetadata);
        }

        Observation latestApproved = observations.stream()
                .max(Comparator.comparing(Observation::getIssuedDateTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        // Prioritize Encounter's approvingPractitioner if set
        Practitioner practitioner = (encounter != null && encounter.getApprovingPractitioner() != null)
                ? encounter.getApprovingPractitioner()
                : (latestApproved != null ? latestApproved.getPerformer() : null);

        if (practitioner == null) {
            return buildNotReadyStatus(
                    "Doctor verifier details are missing. Please re-approve the report.",
                    reportMetadata);
        }

        reportMetadata = buildReportMetadata(serviceRequest, latestApproved != null ? latestApproved.getIssuedDateTime() : OffsetDateTime.now());
        
        Optional<User> approvingUser = userRepository.findByPractitioner_Id(practitioner.getId());
        if (approvingUser.isEmpty()) {
            return buildNotReadyStatus(
                    "Approving practitioner account not found. Please verify approval workflow.",
                    reportMetadata);
        }

        Set<String> allowedRoles = Set.of("PATHOLOGIST", "DOCTOR", "ADMIN");
        boolean approvedByDoctor = approvingUser.get().getRoles() != null
                && approvingUser.get().getRoles().stream()
                .map(r -> r == null ? "" : r.trim().toUpperCase(Locale.ENGLISH).replaceFirst("^ROLE_", ""))
                .anyMatch(allowedRoles::contains);

        if (!approvedByDoctor) {
            return buildNotReadyStatus(
                    "Report must be approved by a doctor/pathologist before generation.",
                    reportMetadata);
        }

        Optional<DiagnosticReport> diagnosticReport = diagnosticReportRepository.findByServiceRequest_Id(serviceRequestId);
        ReportApprovalStatusResponse response = buildNotReadyStatus(
                "Approved and ready for report generation.",
                reportMetadata);
        response.setReady(true);
        response.setApprovedDoctorName(buildPractitionerDisplayName(practitioner));
        response.setApprovedDoctorSignatureImage(imageService != null
                ? imageService.resolveImageUrl(
                        practitioner.getSignatureImageAssetId(),
                        practitioner.getSignatureImage())
                : safe(practitioner.getSignatureImage()));
        response.setApprovedAt(latestApproved != null ? latestApproved.getIssuedDateTime() : OffsetDateTime.now());
        response.setReportStorageReference(diagnosticReport.map(DiagnosticReport::getReportGcsUrl).orElse(null));
        response.setReportLocalValue(diagnosticReport.map(DiagnosticReport::getLocalReportValue).orElse(null));

        String hashid = hashidService.encode(serviceRequestId);
        response.setReportHashId(hashid);
        response.setReportPdfPath("/api/reports/public/" + hashid + "/pdf");
        return response;
    }

    public ReportMetadata buildReportMetadata(ServiceRequest serviceRequest, OffsetDateTime approvedAt) {
        String localReportValue = diagnosticReportRepository.findByServiceRequest_Id(serviceRequest.getId())
                .map(DiagnosticReport::getLocalReportValue)
                .filter(v -> v != null && !v.isBlank())
                .orElseGet(() -> "REP" + serviceRequest.getId());

        String orgCode = Optional.ofNullable(serviceRequest.getPatient())
                .map(Patient::getOrganization)
                .map(Organization::getLocalIdentifierValue)
                .filter(v -> v != null && !v.isBlank())
                .orElse("ORG");

        String normOrg = orgCode.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ENGLISH);
        if (normOrg.length() > 6) normOrg = normOrg.substring(0, 6);
        String normRep = localReportValue.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ENGLISH);
        if (normRep.length() > 12) normRep = normRep.substring(0, 12);

        String ulr = "TC" + normOrg + normRep + "F";
        String accQr = "https://nabl-india.org/accreditation/scope?lab=" + normOrg;
        String intQr = String.format("ULR:%s|SR:%s|REP:%s|AT:%s",
                ulr, serviceRequest.getLocalOrderValue(), localReportValue,
                approvedAt != null ? approvedAt.toString() : "PENDING");

        return new ReportMetadata(ulr, accQr, intQr);
    }

    public record ReportMetadata(String ulrNumber, String accreditationScopeQrContent,
                                 String reportIntegrityQrContent) {}

        private ReportApprovalStatusResponse buildNotReadyStatus(String message, ReportMetadata reportMetadata) {
                ReportApprovalStatusResponse response = new ReportApprovalStatusResponse();
                response.setReady(false);
                response.setMessage(message);
                response.setUlrNumber(reportMetadata.ulrNumber());
                response.setAccreditationScopeQrContent(reportMetadata.accreditationScopeQrContent());
                response.setReportIntegrityQrContent(reportMetadata.reportIntegrityQrContent());
                return response;
        }

    public String buildPractitionerDisplayName(Practitioner p) {
        String full = (safe(p.getPrefix()) + " " + safe(p.getFirstName()) + " " + safe(p.getLastName())).trim();
        return full.isBlank() ? "Doctor" : full.replaceAll("\\s+", " ");
    }

    public String formatDateTime(OffsetDateTime value) {
        if (value == null) return "";
        return value.format(DISPLAY_FMT);
    }

    public String safe(String value) {
        return value == null ? "" : value;
    }
}

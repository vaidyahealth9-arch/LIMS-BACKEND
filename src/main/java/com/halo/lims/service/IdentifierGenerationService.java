package com.halo.lims.service;

import com.halo.lims.repository.*;
import com.halo.lims.model.counter.BillCounter;
import com.halo.lims.model.counter.DiagnosticReportCounter;
import com.halo.lims.model.counter.EncounterValueCounter;
import com.halo.lims.model.counter.PatientMrnCounter;
import com.halo.lims.model.counter.ServiceRequestCounter;
import com.halo.lims.model.counter.SpecimenValueCounter;
import com.halo.lims.model.counter.PractitionerCounter;
import com.halo.lims.model.counter.ObservationCounter;
import com.halo.lims.model.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe, organization-scoped identifier generation service.
 * 
 * PROBLEM SOLVED:
 * Previous identifier generation used count() + 1, which is NOT safe under concurrent loads.
 * This service uses dedicated counter tables per organization per identifier type,
 * with atomic increment operations and automatic retry logic.
 * 
 * GUARANTEES:
 * 1. Organization-scoped uniqueness: each org has independent counter ranges
 * 2. Collision-free: uses database-level locking during counter increment
 * 3. Recoverable: retries automatically on transient failures
 * 4. Deterministic format: identifiers have consistent format across environments
 * 
 * PERFORMANCE:
 * - Single database roundtrip per identifier (counter increment)
 * - Retry backoff prevents thundering herd under sustained conflicts
 * - Logging helps diagnose identifier collisions in production
 * 
 * USAGE:
 *   String mrn = identifierGenerationService.generatePatientMrn(organizationId, 3);
 *   String encounter = identifierGenerationService.generateEncounterValue(organizationId, 3);
 */
@Slf4j
@Service
public class IdentifierGenerationService {

    private final PatientMrnCounterRepository patientMrnCounterRepository;
    private final EncounterValueCounterRepository encounterValueCounterRepository;
    private final ServiceRequestCounterRepository serviceRequestCounterRepository;
    private final SpecimenValueCounterRepository specimenValueCounterRepository;
    private final DiagnosticReportCounterRepository diagnosticReportCounterRepository;
    private final BillCounterRepository billCounterRepository;
    private final OrganizationRepository organizationRepository;
    private final PractitionerCounterRepository practitionerCounterRepository;
    private final ObservationCounterRepository observationCounterRepository;

    private final Map<Integer, String> orgCodeCache = new ConcurrentHashMap<>();

    public IdentifierGenerationService(
            PatientMrnCounterRepository patientMrnCounterRepository,
            EncounterValueCounterRepository encounterValueCounterRepository,
            ServiceRequestCounterRepository serviceRequestCounterRepository,
            SpecimenValueCounterRepository specimenValueCounterRepository,
            DiagnosticReportCounterRepository diagnosticReportCounterRepository,
            BillCounterRepository billCounterRepository,
            OrganizationRepository organizationRepository,
            PractitionerCounterRepository practitionerCounterRepository,
            ObservationCounterRepository observationCounterRepository) {
        this.patientMrnCounterRepository = patientMrnCounterRepository;
        this.encounterValueCounterRepository = encounterValueCounterRepository;
        this.serviceRequestCounterRepository = serviceRequestCounterRepository;
        this.specimenValueCounterRepository = specimenValueCounterRepository;
        this.diagnosticReportCounterRepository = diagnosticReportCounterRepository;
        this.billCounterRepository = billCounterRepository;
        this.organizationRepository = organizationRepository;
        this.practitionerCounterRepository = practitionerCounterRepository;
        this.observationCounterRepository = observationCounterRepository;
    }

    /**
     * Generate a patient MRN with automatic retry on collision.
     * Format: ORG{3}YYMMDD{5}
     * Example: ORG2603101234
     * 
     * @param organizationId Organization ID for org-scoped uniqueness
     * @param maxRetries Maximum retry attempts on collision (typically 3)
     * @return Organization-scoped unique patient MRN
     */
    @Transactional
    public String generatePatientMrn(Integer organizationId, int maxRetries) {
        return generateIdentifierWithRetry(
                "MRN",
                organizationId,
                maxRetries,
                (counter) -> formatPatientMrn(organizationId, counter)
        );
    }

    /**
     * Generate an encounter value with automatic retry on collision.
     * Format: ENC{3}YYMMDD{5}
     * Example: ENC2603101234
     */
    @Transactional
    public String generateEncounterValue(Integer organizationId, int maxRetries) {
        return generateIdentifierWithRetry(
                "ENCOUNTER",
                organizationId,
                maxRetries,
                (counter) -> formatEncounterValue(organizationId, counter)
        );
    }

    /**
     * Generate a service request / order ID with automatic retry on collision.
     * Format: ORD{3}YYMMDD{5}
     * Example: ORD2603101234
     */
    @Transactional
    public String generateOrderValue(Integer organizationId, int maxRetries) {
        return generateIdentifierWithRetry(
                "ORDER",
                organizationId,
                maxRetries,
                (counter) -> formatOrderValue(organizationId, counter)
        );
    }

    /**
     * Generate a specimen value with automatic retry on collision.
     * Format: SPE{3}YYMMDD{5}
     * Example: SPE2603101234
     */
    @Transactional
    public String generateSpecimenValue(Integer organizationId, int maxRetries) {
        return generateIdentifierWithRetry(
                "SPECIMEN",
                organizationId,
                maxRetries,
                (counter) -> formatSpecimenValue(organizationId, counter)
        );
    }

    /**
     * Generate a diagnostic report value with automatic retry on collision.
     * Format: REP{3}YYMMDD{5}
     * Example: REP2603101234
     */
    @Transactional
    public String generateReportValue(Integer organizationId, int maxRetries) {
        return generateIdentifierWithRetry(
                "REPORT",
                organizationId,
                maxRetries,
                (counter) -> formatReportValue(organizationId, counter)
        );
    }

    /**
     * Generate a bill value with automatic retry on collision.
     * Format: BIL{3}YYMMDD{5}
     * Example: BIL2603101234
     */
    @Transactional
    public String generateBillValue(Integer organizationId, int maxRetries) {
        return generateIdentifierWithRetry(
                "BILL",
                organizationId,
                maxRetries,
                (counter) -> formatBillValue(organizationId, counter)
        );
    }

    /**
     * Generate a practitioner ID based on role.
     * Roles: PR (Doctor), TEC (Technician), REP (Receptionist)
     * Format: {Role}{ORG}-{SEQ(5)}
     */
    @Transactional
    public String generatePractitionerId(Integer organizationId, String role, int maxRetries) {
        return generateIdentifierWithRetry(
                "PRACTITIONER",
                organizationId,
                maxRetries,
                (counter) -> formatPractitionerId(organizationId, role, counter)
        );
    }

    /**
     * Generate a high-volume observation ID.
     * Format: OB{ORG}-{YYMMDD}{SEQ(7)}
     */
    @Transactional
    public String generateObservationId(Integer organizationId, int maxRetries) {
        return generateIdentifierWithRetry(
                "OBSERVATION",
                organizationId,
                maxRetries,
                (counter) -> formatObservationId(organizationId, counter)
        );
    }

    /**
     * Generate a test catalog ID based on mnemonic.
     * Format: TC{ORG}-{MNEMONIC} (e.g., TCL1-CBC)
     */
    public String generateTestCatalogId(Integer organizationId, String mnemonic) {
        String orgCode = getOrgCode(organizationId);
        return String.format("TC%s-%s", orgCode, mnemonic.toUpperCase(Locale.ROOT));
    }

    /**
     * Generic retry-enabled identifier generation.
     * Atomically increments counter and formats identifier.
     * On DataIntegrityViolationException (unique constraint), retries with backoff.
     */
    private String generateIdentifierWithRetry(
            String identifierType,
            Integer organizationId,
            int maxRetries,
            IdentifierFormatter formatter) {

        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                // Atomically increment counter and get next value
                long nextCounter = incrementCounter(identifierType, organizationId);
                
                // Format identifier
                String identifier = formatter.format(nextCounter);
                
                log.debug("Generated {} for org {}: {} (attempt {})",
                        identifierType, organizationId, identifier, attempt + 1);
                
                return identifier;
                
            } catch (DataIntegrityViolationException e) {
                attempt++;
                if (attempt >= maxRetries) {
                    log.error("Failed to generate {} after {} retries for org {}. Last error: {}",
                            identifierType, maxRetries, organizationId, e.getMessage());
                    throw e;
                }
                
                // Exponential backoff: 10ms, 20ms, 40ms, etc.
                long backoffMs = 10L * (long) Math.pow(2, attempt - 1);
                log.warn("Identifier collision for {} (org {}), retrying in {}ms (attempt {})",
                        identifierType, organizationId, backoffMs, attempt);
                
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while retrying identifier generation", ie);
                }
            }
        }
        
        throw new RuntimeException(
                String.format("Failed to generate %s for org %s after %d attempts",
                        identifierType, organizationId, maxRetries));
    }

    /**
     * Atomically increment counter for the given identifier type and organization.
     * PostgreSQL FOR UPDATE ensures row-level locking, preventing lost updates.
     */
    private long incrementCounter(String identifierType, Integer organizationId) {
        return switch (identifierType) {
            case "MRN" -> incrementPatientMrnCounter(organizationId);
            case "ENCOUNTER" -> incrementEncounterCounter(organizationId);
            case "ORDER" -> incrementServiceRequestCounter(organizationId);
            case "SPECIMEN" -> incrementSpecimenCounter(organizationId);
            case "REPORT" -> incrementDiagnosticReportCounter(organizationId);
            case "BILL" -> incrementBillCounter(organizationId);
            case "PRACTITIONER" -> incrementPractitionerCounter(organizationId);
            case "OBSERVATION" -> incrementObservationCounter(organizationId);
            default -> throw new IllegalArgumentException("Unknown identifier type: " + identifierType);
        };
    }

    private long incrementPatientMrnCounter(Integer organizationId) {
        PatientMrnCounter counter = patientMrnCounterRepository.findByOrganizationIdForUpdate(organizationId)
                .orElseGet(() -> {
                    PatientMrnCounter created = new PatientMrnCounter();
                    created.setOrganizationId(organizationId);
                    created.setNextCounter(1L);
                    return created;
                });
        long current = counter.getNextCounter() == null ? 1L : counter.getNextCounter();
        counter.setNextCounter(current + 1);
        patientMrnCounterRepository.save(counter);
        return current;
    }

    private long incrementEncounterCounter(Integer organizationId) {
        EncounterValueCounter counter = encounterValueCounterRepository.findByOrganizationIdForUpdate(organizationId)
                .orElseGet(() -> {
                    EncounterValueCounter created = new EncounterValueCounter();
                    created.setOrganizationId(organizationId);
                    created.setNextCounter(1L);
                    return created;
                });
        long current = counter.getNextCounter() == null ? 1L : counter.getNextCounter();
        counter.setNextCounter(current + 1);
        encounterValueCounterRepository.save(counter);
        return current;
    }

    private long incrementServiceRequestCounter(Integer organizationId) {
        ServiceRequestCounter counter = serviceRequestCounterRepository.findByOrganizationIdForUpdate(organizationId)
                .orElseGet(() -> {
                    ServiceRequestCounter created = new ServiceRequestCounter();
                    created.setOrganizationId(organizationId);
                    created.setNextCounter(1L);
                    return created;
                });
        long current = counter.getNextCounter() == null ? 1L : counter.getNextCounter();
        counter.setNextCounter(current + 1);
        serviceRequestCounterRepository.save(counter);
        return current;
    }

    private long incrementSpecimenCounter(Integer organizationId) {
        SpecimenValueCounter counter = specimenValueCounterRepository.findByOrganizationIdForUpdate(organizationId)
                .orElseGet(() -> {
                    SpecimenValueCounter created = new SpecimenValueCounter();
                    created.setOrganizationId(organizationId);
                    created.setNextCounter(1L);
                    return created;
                });
        long current = counter.getNextCounter() == null ? 1L : counter.getNextCounter();
        counter.setNextCounter(current + 1);
        specimenValueCounterRepository.save(counter);
        return current;
    }

    private long incrementDiagnosticReportCounter(Integer organizationId) {
        DiagnosticReportCounter counter = diagnosticReportCounterRepository.findByOrganizationIdForUpdate(organizationId)
                .orElseGet(() -> {
                    DiagnosticReportCounter created = new DiagnosticReportCounter();
                    created.setOrganizationId(organizationId);
                    created.setNextCounter(1L);
                    return created;
                });
        long current = counter.getNextCounter() == null ? 1L : counter.getNextCounter();
        counter.setNextCounter(current + 1);
        diagnosticReportCounterRepository.save(counter);
        return current;
    }

    private long incrementBillCounter(Integer organizationId) {
        BillCounter counter = billCounterRepository.findByOrganizationIdForUpdate(organizationId)
                .orElseGet(() -> {
                    BillCounter created = new BillCounter();
                    created.setOrganizationId(organizationId);
                    created.setNextCounter(1L);
                    return created;
                });
        long current = counter.getNextCounter() == null ? 1L : counter.getNextCounter();
        counter.setNextCounter(current + 1);
        billCounterRepository.save(counter);
        return current;
    }

    private long incrementPractitionerCounter(Integer organizationId) {
        PractitionerCounter counter = practitionerCounterRepository.findByOrganizationIdForUpdate(organizationId)
                .orElseGet(() -> {
                    PractitionerCounter created = new PractitionerCounter();
                    created.setOrganizationId(organizationId);
                    created.setNextCounter(1L);
                    return created;
                });
        long current = counter.getNextCounter() == null ? 1L : counter.getNextCounter();
        counter.setNextCounter(current + 1);
        practitionerCounterRepository.save(counter);
        return current;
    }

    private long incrementObservationCounter(Integer organizationId) {
        ObservationCounter counter = observationCounterRepository.findByOrganizationIdForUpdate(organizationId)
                .orElseGet(() -> {
                    ObservationCounter created = new ObservationCounter();
                    created.setOrganizationId(organizationId);
                    created.setNextCounter(1L);
                    return created;
                });
        long current = counter.getNextCounter() == null ? 1L : counter.getNextCounter();
        counter.setNextCounter(current + 1);
        observationCounterRepository.save(counter);
        return current;
    }

    // Formatting methods optimized for human-readability and short length

    private String getOrgCode(Integer orgId) {
        return orgCodeCache.computeIfAbsent(orgId, id -> {
            Organization org = organizationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Organization not found: " + id));
            return org.getLocalIdentifierValue();
        });
    }

    private String getFormattedDate(String pattern) {
        return OffsetDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    private String formatPatientMrn(Integer orgId, long counter) {
        // Format: P{ORG}-{YY}{SEQ(5)} (e.g., PL1-2600123)
        String orgCode = getOrgCode(orgId);
        String year = getFormattedDate("yy");
        return String.format("P%s-%s%05d", orgCode, year, counter);
    }

    private String formatEncounterValue(Integer orgId, long counter) {
        // Format: E{ORG}-{MMDD}{SEQ(5)} (e.g., EL1-040200450)
        String orgCode = getOrgCode(orgId);
        String date = getFormattedDate("MMdd");
        return String.format("E%s-%s%05d", orgCode, date, counter);
    }

    private String formatOrderValue(Integer orgId, long counter) {
        // Format: O{ORG}-{YY}{SEQ(5)} (Following Patient/Invoice pattern)
        String orgCode = getOrgCode(orgId);
        String year = getFormattedDate("yy");
        return String.format("O%s-%s%05d", orgCode, year, counter);
    }

    private String formatSpecimenValue(Integer orgId, long counter) {
        // Format: S{ORG}-{MMDD}{SEQ(5)} (e.g., SL1-020600450)
        String orgCode = getOrgCode(orgId);
        String date = getFormattedDate("MMdd");
        return String.format("S%s-%s%05d", orgCode, date, counter);
    }

    private String formatReportValue(Integer orgId, long counter) {
        // Format: R{ORG}-{YYMMDD}-{SEQ(5)} (e.g., RL1-260418-00120)
        String orgCode = getOrgCode(orgId);
        String date = getFormattedDate("yyMMdd");
        return String.format("R%s-%s-%05d", orgCode, date, counter);
    }

    private String formatBillValue(Integer orgId, long counter) {
        // Format: I{ORG}-{YY}{SEQ(5)} (e.g., IL1-2605001)
        String orgCode = getOrgCode(orgId);
        String year = getFormattedDate("yy");
        return String.format("I%s-%s%05d", orgCode, year, counter);
    }

    private String formatPractitionerId(Integer orgId, String role, long counter) {
        // Format: {Role}{ORG}-{SEQ(5)} (e.g., TECL1-00124)
        String orgCode = getOrgCode(orgId);
        return String.format("%s%s-%05d", role.toUpperCase(Locale.ROOT), orgCode, counter);
    }

    private String formatObservationId(Integer orgId, long counter) {
        // Format: OB{ORG}-{YYMMDD}{SEQ(7)} (e.g., OBL1-2604180005000)
        String orgCode = getOrgCode(orgId);
        String date = getFormattedDate("yyMMdd");
        return String.format("OB%s-%s%07d", orgCode, date, counter);
    }

    private String formatCounter(long counter, int width) {
        // No longer used for main identifiers but kept for utility
        String counterStr = Long.toString(counter, 36).toUpperCase(Locale.ROOT);
        while (counterStr.length() < width) {
            counterStr = "0" + counterStr;
        }
        if (counterStr.length() > width) {
            counterStr = counterStr.substring(counterStr.length() - width);
        }
        return counterStr;
    }

    /**
     * Functional interface for identifier formatting.
     * Allows extensibility for custom formatting strategies.
     */
    @FunctionalInterface
    interface IdentifierFormatter {
        String format(long counter);
    }
}

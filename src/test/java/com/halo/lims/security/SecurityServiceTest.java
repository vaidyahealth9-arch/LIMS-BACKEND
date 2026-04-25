package com.halo.lims.security;

import com.halo.lims.model.Encounter;
import com.halo.lims.model.Organization;
import com.halo.lims.model.Patient;
import com.halo.lims.model.User;
import com.halo.lims.repository.BillRepository;
import com.halo.lims.repository.EncounterRepository;
import com.halo.lims.repository.ObservationRepository;
import com.halo.lims.repository.PatientRepository;
import com.halo.lims.repository.ServiceRequestRepository;
import com.halo.lims.repository.SpecimenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityServiceTest {

    private final PatientRepository patientRepository = mock(PatientRepository.class);
    private final ServiceRequestRepository serviceRequestRepository = mock(ServiceRequestRepository.class);
    private final ObservationRepository observationRepository = mock(ObservationRepository.class);
    private final EncounterRepository encounterRepository = mock(EncounterRepository.class);
    private final SpecimenRepository specimenRepository = mock(SpecimenRepository.class);
    private final BillRepository billRepository = mock(BillRepository.class);

    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        securityService = new SecurityService(
                patientRepository,
                serviceRequestRepository,
                observationRepository,
                encounterRepository,
                specimenRepository,
                billRepository
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void canAccessEncounter_usesServiceProviderOrganizationWhenPresent() {
        Organization userOrg = Organization.builder().id(11).organizationName("Lab Org").build();
        Organization serviceProviderOrg = Organization.builder().id(11).organizationName("Lab Org").build();
        Organization patientOrg = Organization.builder().id(22).organizationName("Patient Org").build();

        User currentUser = User.builder()
                .id(1)
                .username("doctor.user")
                .password("x")
                .roles(Set.of("DOCTOR"))
                .organization(userOrg)
                .isActive(true)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities())
        );

        Patient patient = Patient.builder().id(100).organization(patientOrg).build();
        Encounter encounter = Encounter.builder()
                .id(200)
                .patient(patient)
                .serviceProvider(serviceProviderOrg)
                .build();

        when(encounterRepository.findById(200)).thenReturn(Optional.of(encounter));

        assertTrue(securityService.canAccessEncounter(200));
    }
}

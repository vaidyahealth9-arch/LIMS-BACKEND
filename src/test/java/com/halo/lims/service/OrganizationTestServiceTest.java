package com.halo.lims.service;

import com.halo.lims.dto.organization.test.OrganizationTestRequest;
import com.halo.lims.dto.organization.test.OrganizationTestResponse;
import com.halo.lims.model.Organization;
import com.halo.lims.model.OrganizationTest;
import com.halo.lims.model.TestAnalyte;
import com.halo.lims.repository.OrganizationRepository;
import com.halo.lims.repository.OrganizationTestAnalyteRepository;
import com.halo.lims.repository.OrganizationTestRepository;
import com.halo.lims.repository.TestAnalyteRepository;
import com.halo.lims.repository.TestRepository;
import com.halo.lims.security.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrganizationTestServiceTest {

    @InjectMocks
    private OrganizationTestService organizationTestService;

    @Mock
    private OrganizationTestRepository organizationTestRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private TestRepository testRepository;

    @Mock
    private OrganizationTestAnalyteRepository organizationTestAnalyteRepository;

    @Mock
    private TestAnalyteRepository testAnalyteRepository;

    @Mock
    private SecurityService securityService;

    private Organization organization;
    private com.halo.lims.model.Test test;
    private OrganizationTest organizationTest;
    private OrganizationTestRequest organizationTestRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        organization = new Organization();
        organization.setId(1);
        organization.setOrganizationName("Test Org");

        test = new com.halo.lims.model.Test();
        test.setId(1);
        test.setTestName("Test Test");

        organizationTest = new OrganizationTest();
        organizationTest.setOrganization(organization);
        organizationTest.setTest(test);
        organizationTest.setPrice(new BigDecimal("100.00"));

        organizationTestRequest = new OrganizationTestRequest();
        organizationTestRequest.setTestId(1);
        organizationTestRequest.setIsEnabled(true);
        organizationTestRequest.setPrice(new BigDecimal("150.00"));
    }

    @Test
    void whenGetOrCreateOrganizationTest_andTestExists_thenReturnTest() {
        when(organizationTestRepository.findByOrganization_IdAndTest_Id(1, 1)).thenReturn(Optional.of(organizationTest));

        OrganizationTest result = organizationTestService.getOrCreateOrganizationTest(1, 1);

        assertNotNull(result);
        assertEquals(1, result.getOrganization().getId());
        assertEquals(1, result.getTest().getId());
    }

    @Test
    void whenGetOrCreateOrganizationTest_andTestDoesNotExist_thenCreateAndReturnTest() {
        when(organizationTestRepository.findByOrganization_IdAndTest_Id(1, 1)).thenReturn(Optional.empty());
        when(organizationRepository.findById(1)).thenReturn(Optional.of(organization));
        when(testRepository.findById(1)).thenReturn(Optional.of(test));
        when(organizationTestRepository.save(any(OrganizationTest.class))).thenReturn(organizationTest);

        OrganizationTest result = organizationTestService.getOrCreateOrganizationTest(1, 1);

        assertNotNull(result);
        assertEquals(1, result.getOrganization().getId());
        assertEquals(1, result.getTest().getId());
        verify(organizationTestRepository).save(any(OrganizationTest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenAddOrUpdateOrganizationTest_thenUpdateAndReturnResponse() {
        when(securityService.isUserInOrganization(1)).thenReturn(true);
        when(organizationTestRepository.findByOrganization_IdAndTest_Id(1, 1)).thenReturn(Optional.of(organizationTest));
        when(organizationTestRepository.save(any(OrganizationTest.class))).thenReturn(organizationTest);

        OrganizationTestResponse response = organizationTestService.addOrUpdateOrganizationTest(1, organizationTestRequest);

        assertNotNull(response);
        assertEquals(1, response.getTestId());
        assertEquals(new BigDecimal("150.00"), response.getPrice());
    }
}

package com.halo.lims.service;

import com.halo.lims.dto.AnalyteResponseDto;
import com.halo.lims.dto.UpsertOrgAnalyteRequestDto;
import com.halo.lims.model.Organization;
import com.halo.lims.model.OrganizationTestAnalyte;
import com.halo.lims.repository.OrganizationRepository;
import com.halo.lims.repository.OrganizationTestAnalyteRepository;
import com.halo.lims.repository.ReferenceRangeRepository;
import com.halo.lims.repository.TestAnalyteRepository;
import com.halo.lims.service.impl.OrganizationAnalyteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class OrganizationAnalyteServiceTest {

    @Mock
    private TestAnalyteRepository testAnalyteRepository;

    @Mock
    private OrganizationTestAnalyteRepository organizationTestAnalyteRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private ReferenceRangeRepository referenceRangeRepository;

    @InjectMocks
    private OrganizationAnalyteServiceImpl organizationAnalyteService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAnalytesForOrganization() {
        // Given
        Integer organizationId = 1;

        Organization organization = new Organization();
        organization.setId(organizationId);

        com.halo.lims.model.Test parentTest = new com.halo.lims.model.Test();
        parentTest.setTestName("Parent Test");

        com.halo.lims.model.TestAnalyte testAnalyte = new com.halo.lims.model.TestAnalyte();
        testAnalyte.setId(1);
        testAnalyte.setAnalyteName("Test Analyte");
        testAnalyte.setParentTest(parentTest);

        when(testAnalyteRepository.findAll()).thenReturn(Collections.singletonList(testAnalyte));
        when(organizationTestAnalyteRepository.findByOrganizationId(organizationId)).thenReturn(Collections.emptyList());
        when(referenceRangeRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<AnalyteResponseDto> result = organizationAnalyteService.getAnalytesForOrganization(organizationId);

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Analyte", result.get(0).getName());
    }

    @Test
    public void testUpsertAnalyteForOrganization() {
        // Given
        Integer organizationId = 1;
        Integer analyteId = 1;

        Organization organization = new Organization();
        organization.setId(organizationId);

        com.halo.lims.model.TestAnalyte testAnalyte = new com.halo.lims.model.TestAnalyte();
        testAnalyte.setId(analyteId);

        UpsertOrgAnalyteRequestDto request = new UpsertOrgAnalyteRequestDto();
        request.setAnalyteId(analyteId);
        request.setPrice(10.0);
        request.setCode("CODE");
        request.setBioReference("BIO");

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(testAnalyteRepository.findById(analyteId)).thenReturn(Optional.of(testAnalyte));
        when(organizationTestAnalyteRepository.findByOrganizationIdAndTestAnalyteId(organizationId, analyteId)).thenReturn(Optional.empty());
        when(organizationTestAnalyteRepository.save(any(OrganizationTestAnalyte.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        organizationAnalyteService.upsertAnalyteForOrganization(organizationId, request);

        // Then
        // No exception thrown
    }
}

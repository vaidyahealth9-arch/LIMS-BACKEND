package com.halo.lims.controller;

import com.halo.lims.dto.AnalyteResponseDto;
import com.halo.lims.dto.UpsertOrgAnalyteRequestDto;
import com.halo.lims.service.OrganizationAnalyteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class OrganizationAnalyteControllerTest {

    @Mock
    private OrganizationAnalyteService organizationAnalyteService;

    @InjectMocks
    private OrganizationAnalyteController organizationAnalyteController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAnalytesForOrganization() {
        // Given
        Integer organizationId = 1;
        AnalyteResponseDto analyteResponseDto = new AnalyteResponseDto();
        analyteResponseDto.setName("Test Analyte");

        when(organizationAnalyteService.getAnalytesForOrganization(organizationId)).thenReturn(Collections.singletonList(analyteResponseDto));

        // When
        ResponseEntity<List<AnalyteResponseDto>> response = organizationAnalyteController.getAnalytesForOrganization(organizationId);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("Test Analyte", response.getBody().get(0).getName());
    }

    @Test
    public void testUpsertAnalyteForOrganization() {
        // Given
        Integer organizationId = 1;
        UpsertOrgAnalyteRequestDto request = new UpsertOrgAnalyteRequestDto();
        request.setAnalyteId(1);
        request.setPrice(10.0);
        request.setCode("CODE");
        request.setBioReference("BIO");

        // When
        ResponseEntity<Void> response = organizationAnalyteController.upsertAnalyteForOrganization(organizationId, request);

        // Then
        assertEquals(200, response.getStatusCodeValue());
    }
}

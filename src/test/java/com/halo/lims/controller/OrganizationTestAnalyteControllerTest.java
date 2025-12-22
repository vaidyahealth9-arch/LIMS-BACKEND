package com.halo.lims.controller;

import com.halo.lims.dto.organizationTestAnalyte.BulkUpdateOrganizationTestAnalyteRequest;
import com.halo.lims.service.OrganizationTestAnalyteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

public class OrganizationTestAnalyteControllerTest {

    @Mock
    private OrganizationTestAnalyteService organizationTestAnalyteService;

    @InjectMocks
    private OrganizationTestAnalyteController organizationTestAnalyteController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testBulkUpdateOrganizationTestAnalytes() {
        BulkUpdateOrganizationTestAnalyteRequest request = new BulkUpdateOrganizationTestAnalyteRequest();
        request.setAnalyteIds(Collections.singletonList(1));

        ResponseEntity<Void> response = organizationTestAnalyteController.bulkUpdateOrganizationTestAnalytes(1, 1, request);

        assertEquals(200, response.getStatusCodeValue());
        verify(organizationTestAnalyteService).bulkUpdateOrganizationTestAnalytes(1, 1, Collections.singletonList(1));
    }
}

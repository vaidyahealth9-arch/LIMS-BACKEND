package com.halo.lims.controller;

import com.halo.lims.dto.organization.test.OrganizationTestRequest;
import com.halo.lims.dto.organization.test.OrganizationTestResponse;
import com.halo.lims.service.OrganizationTestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

public class OrganizationTestControllerTest {

    @Mock
    private OrganizationTestService organizationTestService;

    @InjectMocks
    private OrganizationTestController organizationTestController;

    private OrganizationTestRequest organizationTestRequest;
    private OrganizationTestResponse organizationTestResponse;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        organizationTestRequest = new OrganizationTestRequest();
        organizationTestRequest.setTestId(1);
        organizationTestRequest.setIsEnabled(true);
        organizationTestRequest.setPrice(new BigDecimal("100.00"));

        organizationTestResponse = new OrganizationTestResponse();
        organizationTestResponse.setTestId(1);
        organizationTestResponse.setIsEnabled(true);
        organizationTestResponse.setPrice(new BigDecimal("100.00"));
    }

    @Test
    public void testAddOrUpdateOrganizationTest() {
        when(organizationTestService.addOrUpdateOrganizationTest(anyInt(), any(OrganizationTestRequest.class)))
                .thenReturn(organizationTestResponse);

        ResponseEntity<OrganizationTestResponse> response = organizationTestController.addOrUpdateOrganizationTest(1, organizationTestRequest);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(1, response.getBody().getTestId());
        assertEquals(true, response.getBody().getIsEnabled());
        assertEquals(new BigDecimal("100.00"), response.getBody().getPrice());
    }
}

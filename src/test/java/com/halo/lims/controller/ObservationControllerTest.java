package com.halo.lims.controller;

import com.halo.lims.dto.observation.ObservationResponse;
import com.halo.lims.security.CustomUserDetailsService;
import com.halo.lims.security.JwtUtil;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.ObservationService;
import com.halo.lims.service.ServiceRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import com.halo.lims.dto.observation.ObservationResponse;
import com.halo.lims.security.CustomUserDetailsService;
import com.halo.lims.security.JwtUtil;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.ObservationService;
import com.halo.lims.service.ServiceRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import com.halo.lims.dto.observation.ObservationResponse;
import com.halo.lims.security.CustomUserDetailsService;
import com.halo.lims.security.JwtUtil;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.ObservationService;
import com.halo.lims.dto.observation.ObservationResponse;
import com.halo.lims.security.CustomUserDetailsService;
import com.halo.lims.security.JwtRequestFilter;
import com.halo.lims.security.JwtUtil;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.ObservationService;
import com.halo.lims.service.ServiceRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "spring.cloud.gcp.sql.enabled=false",
    "spring.cloud.gcp.secretmanager.enabled=false"
})
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtRequestFilter.class))
public class ObservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ObservationService observationService;

    @MockBean
    private ServiceRequestService serviceRequestService;

    @MockBean
    private SecurityService securityService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getObservationsByServiceRequestId_shouldReturnObservations() throws Exception {
        ObservationResponse obs1 = new ObservationResponse();
        obs1.setId("obs-001");
        obs1.setServiceRequestId("567");
        obs1.setSpecimenId("spec-123");
        obs1.setTestName("Urine Protein Creatinine Ratio - Spot");
        obs1.setAnalyteId("an-101");
        obs1.setAnalyteName("Urine Protein");
        obs1.setValueNumeric(new BigDecimal("89.80"));
        obs1.setUnit("mg/dL");
        obs1.setReferenceRange("1 - 14");
        obs1.setInterpretation("Under most circumstances untimed (spot) urine samples be used to detect and monitor proteinuria in children and adults.");
        obs1.setEffectiveDateTime(OffsetDateTime.parse("2025-09-21T10:55:00.000Z"));

        List<ObservationResponse> response = Arrays.asList(obs1);

        given(observationService.getObservationsByServiceRequestId(567)).willReturn(response);
        given(securityService.canAccessServiceRequest(anyInt())).willReturn(true);

        mockMvc.perform(get("/api/service-requests/567/observations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("obs-001"))
                .andExpect(jsonPath("$[0].analyteName").value("Urine Protein"));
    }
}

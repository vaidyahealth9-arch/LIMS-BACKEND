package com.halo.lims.controller;

import com.halo.lims.dto.encounter.EncounterDetailResponse;
import com.halo.lims.security.CustomUserDetailsService;
import com.halo.lims.security.JwtRequestFilter;
import com.halo.lims.security.JwtUtil;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.EncounterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyInt;
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
public class EncounterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EncounterService encounterService;

    @MockitoBean
    private SecurityService securityService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getEncounterDetailsById_shouldReturnEncounterDetails() throws Exception {
        EncounterDetailResponse response = new EncounterDetailResponse();
        response.setId(1);
        response.setPatientId(1);
        response.setPatientName("Mr. D Sandeep Kumar");
        response.setPatientAge("39 Years");
        response.setPatientGender("Male");
        response.setMrnId("MRN78910");
        response.setReferenceDoctor("BHARAT HEAVY ELECTRICALS LIMITED");
        response.setDate(OffsetDateTime.parse("2025-09-21T06:38:00.000Z"));
        response.setCollectionDate(OffsetDateTime.parse("2025-09-21T06:42:00.000Z"));
        response.setSampleType("Urine");
        response.setStatus("COMPLETED");
        response.setLocalEncounterValue("250330042234");
        response.setTests(Arrays.asList("Urine Protein Creatinine Ratio - Spot"));
        response.setServiceRequestIds(Arrays.asList(567));

        given(encounterService.getEncounterDetailsById(1)).willReturn(response);
        given(securityService.canAccessEncounter(anyInt())).willReturn(true);

        mockMvc.perform(get("/api/encounters/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patientName").value("Mr. D Sandeep Kumar"));
    }

    @Test
    @WithMockUser(roles = "PATHOLOGIST")
    public void getEncounterDetailsById_shouldAllowPathologist() throws Exception {
        EncounterDetailResponse response = new EncounterDetailResponse();
        response.setId(1);
        response.setPatientId(1);
        response.setPatientName("Mr. D Sandeep Kumar");
        response.setStatus("COMPLETED");

        given(encounterService.getEncounterDetailsById(1)).willReturn(response);
        given(securityService.canAccessEncounter(anyInt())).willReturn(true);

        mockMvc.perform(get("/api/encounters/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patientName").value("Mr. D Sandeep Kumar"));
    }
}

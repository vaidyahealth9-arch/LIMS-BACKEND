package com.halo.lims.controller;

import com.halo.lims.dto.patient.PatientRegistrationResponse;
import com.halo.lims.security.CustomUserDetailsService;
import com.halo.lims.security.JwtRequestFilter;
import com.halo.lims.security.JwtUtil;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.PatientService;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.gcp.sql.enabled=false",
        "spring.cloud.gcp.secretmanager.enabled=false",
        "spring.cloud.gcp.storage.enabled=false"
})
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtRequestFilter.class))
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    @MockitoBean
    private SecurityService securityService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void lookupPatientFromPhr_shouldReturnPrefillPayload() throws Exception {
        PatientRegistrationResponse response = new PatientRegistrationResponse();
        response.setId(42);
        response.setFirstName("Pooja");
        response.setLastName("Sharma");
        response.setGender("female");
        response.setDateOfBirth(LocalDate.of(1995, 3, 22));
        response.setContactPhone("9876543210");
        response.setContactEmail("pooja.sharma@email.com");
        response.setAddressLine1("123 Main St");
        response.setCity("Hyderabad");
        response.setState("Telangana");
        response.setPostalCode("500001");

        given(patientService.findPatientByMobile("9876543210", null)).willReturn(Optional.of(response));

        mockMvc.perform(get("/api/patients/phr-lookup").param("mobile", "9876543210"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Pooja"))
                .andExpect(jsonPath("$.lastName").value("Sharma"))
                .andExpect(jsonPath("$.contactPhone").value("9876543210"));
    }
}
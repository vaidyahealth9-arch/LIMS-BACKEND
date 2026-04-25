package com.halo.lims.controller;

import com.halo.lims.dto.report.ReportApprovalStatusResponse;
import com.halo.lims.dto.report.ReportPdfDeletionResponse;
import com.halo.lims.security.CustomUserDetailsService;
import com.halo.lims.security.JwtRequestFilter;
import com.halo.lims.security.JwtUtil;
import com.halo.lims.security.SecurityService;
import com.halo.lims.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.gcp.sql.enabled=false",
        "spring.cloud.gcp.secretmanager.enabled=false",
        "spring.cloud.gcp.storage.enabled=false",
        "app.report.delete.enabled=true"
})
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtRequestFilter.class))
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private SecurityService securityService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "DOCTOR")
    void downloadUnifiedReport_shouldReturnHtmlAttachment() throws Exception {
        given(securityService.canAccessServiceRequest(anyInt())).willReturn(true);
        given(reportService.getReportApprovalStatus(anyInt()))
                .willReturn(ReportApprovalStatusResponse.builder()
                        .ready(true)
                        .message("Approved")
                        .build());
        given(reportService.buildUnifiedHtmlReport(anyInt(), anyBoolean(), anyString()))
                .willReturn("<html><body><h1>Unified Diagnostic Report</h1></body></html>");

        mockMvc.perform(get("/api/reports/download/567").param("withHeader", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/html"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=unified-report-567.html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unified Diagnostic Report")));
    }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteUnifiedReportPdf_shouldReturnDeletionResponse() throws Exception {
                given(securityService.canAccessServiceRequest(anyInt())).willReturn(true);
                given(reportService.deleteStoredPdfReport(567))
                                .willReturn(new ReportPdfDeletionResponse(567, true, "local://reports/1/REP63.pdf", "Deleted stored PDF reference for service request 567"));

                mockMvc.perform(delete("/api/reports/pdf/567"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"deleted\":true")))
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("REP63.pdf")));
        }
}

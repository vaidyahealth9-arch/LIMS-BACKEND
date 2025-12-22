package com.halo.lims.controller;

import com.halo.lims.dto.AnalyteResponseDto;
import com.halo.lims.dto.UpsertOrgAnalyteRequestDto;
import com.halo.lims.service.OrganizationAnalyteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/organizations/{organizationId}/analytes")
@RequiredArgsConstructor
public class OrganizationAnalyteController {

    private final OrganizationAnalyteService organizationAnalyteService;

    @GetMapping
    public ResponseEntity<List<AnalyteResponseDto>> getAnalytesForOrganization(@PathVariable Integer organizationId) {
        List<AnalyteResponseDto> analytes = organizationAnalyteService.getAnalytesForOrganization(organizationId);
        return ResponseEntity.ok(analytes);
    }

    @PostMapping
    public ResponseEntity<Void> upsertAnalyteForOrganization(@PathVariable Integer organizationId, @RequestBody UpsertOrgAnalyteRequestDto request) {
        organizationAnalyteService.upsertAnalyteForOrganization(organizationId, request);
        return ResponseEntity.ok().build();
    }
}

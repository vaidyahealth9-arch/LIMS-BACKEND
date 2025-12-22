package com.halo.lims.service;

import com.halo.lims.dto.AnalyteResponseDto;
import com.halo.lims.dto.UpsertOrgAnalyteRequestDto;

import java.util.List;

public interface OrganizationAnalyteService {
    List<AnalyteResponseDto> getAnalytesForOrganization(Integer organizationId);
    void upsertAnalyteForOrganization(Integer organizationId, UpsertOrgAnalyteRequestDto request);
}

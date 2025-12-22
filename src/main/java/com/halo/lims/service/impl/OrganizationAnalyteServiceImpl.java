package com.halo.lims.service.impl;

import com.halo.lims.dto.AnalyteResponseDto;
import com.halo.lims.dto.UpsertOrgAnalyteRequestDto;
import com.halo.lims.model.Organization;
import com.halo.lims.model.OrganizationTestAnalyte;
import com.halo.lims.model.TestAnalyte;
import com.halo.lims.repository.OrganizationRepository;
import com.halo.lims.repository.OrganizationTestAnalyteRepository;
import com.halo.lims.repository.TestAnalyteRepository;
import com.halo.lims.service.OrganizationAnalyteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationAnalyteServiceImpl implements OrganizationAnalyteService {

    private final TestAnalyteRepository testAnalyteRepository;
    private final OrganizationTestAnalyteRepository organizationTestAnalyteRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    public List<AnalyteResponseDto> getAnalytesForOrganization(Integer organizationId) {
        List<TestAnalyte> allAnalytes = testAnalyteRepository.findAll();
        List<OrganizationTestAnalyte> orgAnalytes = organizationTestAnalyteRepository.findByOrganizationId(organizationId);

        Map<Integer, OrganizationTestAnalyte> orgAnalyteMap = orgAnalytes.stream()
                .collect(Collectors.toMap(ota -> ota.getTestAnalyte().getId(), ota -> ota));

        return allAnalytes.stream()
                .map(analyte -> {
                    AnalyteResponseDto dto = new AnalyteResponseDto();
                    dto.setId(analyte.getId().longValue());
                    dto.setName(analyte.getAnalyteName());
                    if (analyte.getParentTest() != null) {
                        dto.setAssociatedTest(analyte.getParentTest().getTestName());
                    }

                    if (orgAnalyteMap.containsKey(analyte.getId())) {
                        OrganizationTestAnalyte orgAnalyte = orgAnalyteMap.get(analyte.getId());
                        dto.setPrice(orgAnalyte.getPrice());
                        dto.setCode(orgAnalyte.getCode());
                        dto.setBioReference(orgAnalyte.getBiologicalRefInterval());
                        dto.setOrgSpecific(true);
                    } else {
                        dto.setPrice(null);
                        dto.setCode(analyte.getAnalyteCode());
                        dto.setBioReference(analyte.getBiologicalRefInterval());
                        dto.setOrgSpecific(false);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void upsertAnalyteForOrganization(Integer organizationId, UpsertOrgAnalyteRequestDto request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        TestAnalyte testAnalyte = testAnalyteRepository.findById(request.getAnalyteId())
                .orElseThrow(() -> new RuntimeException("TestAnalyte not found"));

        Optional<OrganizationTestAnalyte> existingOrgAnalyteOpt = organizationTestAnalyteRepository.findByOrganizationIdAndTestAnalyteId(organizationId, request.getAnalyteId());

        OrganizationTestAnalyte orgAnalyte;
        if (existingOrgAnalyteOpt.isPresent()) {
            orgAnalyte = existingOrgAnalyteOpt.get();
        } else {
            orgAnalyte = new OrganizationTestAnalyte();
            orgAnalyte.setOrganization(organization);
            orgAnalyte.setTestAnalyte(testAnalyte);
        }

        orgAnalyte.setPrice(request.getPrice());
        orgAnalyte.setCode(request.getCode());
        orgAnalyte.setBiologicalRefInterval(request.getBioReference());

        organizationTestAnalyteRepository.save(orgAnalyte);
    }
}

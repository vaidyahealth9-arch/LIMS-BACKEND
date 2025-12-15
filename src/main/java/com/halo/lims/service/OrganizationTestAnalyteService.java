package com.halo.lims.service;

import com.halo.lims.dto.organizationTestAnalyte.CreateOrganizationTestAnalyteRequest;
import com.halo.lims.dto.organizationTestAnalyte.OrganizationTestAnalyteDto;
import com.halo.lims.dto.organizationTestAnalyte.UpdateOrganizationTestAnalyteRequest;
import com.halo.lims.model.Organization;
import com.halo.lims.model.OrganizationTestAnalyte;
import com.halo.lims.model.TestAnalyte;
import com.halo.lims.model.compositeKeys.OrganizationTestAnalyteId;
import com.halo.lims.repository.OrganizationRepository;
import com.halo.lims.repository.OrganizationTestAnalyteRepository;
import com.halo.lims.repository.TestAnalyteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationTestAnalyteService {

    private final OrganizationTestAnalyteRepository organizationTestAnalyteRepository;
    private final OrganizationRepository organizationRepository;
    private final TestAnalyteRepository testAnalyteRepository;

    public OrganizationTestAnalyteDto createOrganizationTestAnalyte(CreateOrganizationTestAnalyteRequest request) {
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        TestAnalyte testAnalyte = testAnalyteRepository.findById(request.getTestAnalyteId())
                .orElseThrow(() -> new RuntimeException("TestAnalyte not found"));

        OrganizationTestAnalyte organizationTestAnalyte = OrganizationTestAnalyte.builder()
                .organization(organization)
                .testAnalyte(testAnalyte)
                .resultType(request.getResultType())
                .decimalPlaces(request.getDecimalPlaces())
                .biologicalRefInterval(request.getBiologicalRefInterval())
                .build();

        organizationTestAnalyte = organizationTestAnalyteRepository.save(organizationTestAnalyte);
        return toDto(organizationTestAnalyte);
    }

    public OrganizationTestAnalyteDto getOrganizationTestAnalyte(Integer organizationId, Integer testAnalyteId) {
        OrganizationTestAnalyteId id = new OrganizationTestAnalyteId(organizationId, testAnalyteId);
        return organizationTestAnalyteRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    public List<OrganizationTestAnalyteDto> getAllOrganizationTestAnalytes() {
        return organizationTestAnalyteRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public OrganizationTestAnalyteDto updateOrganizationTestAnalyte(Integer organizationId, Integer testAnalyteId, UpdateOrganizationTestAnalyteRequest request) {
        OrganizationTestAnalyteId id = new OrganizationTestAnalyteId(organizationId, testAnalyteId);
        OrganizationTestAnalyte organizationTestAnalyte = organizationTestAnalyteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrganizationTestAnalyte not found"));

        organizationTestAnalyte.setResultType(request.getResultType());
        organizationTestAnalyte.setDecimalPlaces(request.getDecimalPlaces());
        organizationTestAnalyte.setBiologicalRefInterval(request.getBiologicalRefInterval());

        organizationTestAnalyte = organizationTestAnalyteRepository.save(organizationTestAnalyte);
        return toDto(organizationTestAnalyte);
    }

    public void deleteOrganizationTestAnalyte(Integer organizationId, Integer testAnalyteId) {
        OrganizationTestAnalyteId id = new OrganizationTestAnalyteId(organizationId, testAnalyteId);
        organizationTestAnalyteRepository.deleteById(id);
    }

    private OrganizationTestAnalyteDto toDto(OrganizationTestAnalyte organizationTestAnalyte) {
        OrganizationTestAnalyteDto dto = new OrganizationTestAnalyteDto();
        dto.setOrganizationId(organizationTestAnalyte.getOrganization().getId());
        dto.setTestAnalyteId(organizationTestAnalyte.getTestAnalyte().getId());
        dto.setResultType(organizationTestAnalyte.getResultType());
        dto.setDecimalPlaces(organizationTestAnalyte.getDecimalPlaces());
        dto.setBiologicalRefInterval(organizationTestAnalyte.getBiologicalRefInterval());
        return dto;
    }
}

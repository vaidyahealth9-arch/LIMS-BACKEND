package com.halo.lims.service;

import com.halo.lims.dto.organization.OrganizationCreateRequest;
import com.halo.lims.dto.organization.OrganizationResponse;
import com.halo.lims.model.Organization;
import com.halo.lims.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    /**
     * Creates a new organization (e.g., a new lab).
     * @param request The DTO containing organization details.
     * @return The created OrganizationResponse.
     */
    @Transactional
    public OrganizationResponse createOrganization(OrganizationCreateRequest request) {
        if (organizationRepository.findByLocalIdentifierValue(request.getLocalIdentifierValue()).isPresent()) {
            throw new IllegalArgumentException("Organization with local identifier value " + request.getLocalIdentifierValue() + " already exists.");
        }

        Organization organization = Organization.builder()
                .organizationName(request.getOrganizationName())
                .orgType(request.getOrgType())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .abdmFacilityId(request.getAbdmFacilityId())
                .localIdentifierSystem("http://com.lims/organization-id") // Define your LIMS Organization ID system URI
                .localIdentifierValue(request.getLocalIdentifierValue())
                .build();

        Organization savedOrganization = organizationRepository.save(organization);
        return mapToOrganizationResponse(savedOrganization);
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(Integer id) {
        return organizationRepository.findById(id)
                .map(this::mapToOrganizationResponse)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(this::mapToOrganizationResponse)
                .collect(Collectors.toList());
    }

    private OrganizationResponse mapToOrganizationResponse(Organization organization) {
        OrganizationResponse response = new OrganizationResponse();
        response.setId(organization.getId());
        response.setOrganizationName(organization.getOrganizationName());
        response.setOrgType(organization.getOrgType());
        response.setContactPhone(organization.getContactPhone());
        response.setContactEmail(organization.getContactEmail());
        response.setAddressLine1(organization.getAddressLine1());
        response.setCity(organization.getCity());
        response.setState(organization.getState());
        response.setPostalCode(organization.getPostalCode());
        response.setCountry(organization.getCountry());
        response.setAbdmFacilityId(organization.getAbdmFacilityId());
        response.setLocalIdentifierValue(organization.getLocalIdentifierValue());
        response.setCreatedAt(organization.getCreatedAt());
        response.setUpdatedAt(organization.getUpdatedAt());
        return response;
    }
}

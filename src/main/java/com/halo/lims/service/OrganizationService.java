package com.halo.lims.service;

import com.halo.lims.dto.organization.OrganizationCreateRequest;
import com.halo.lims.dto.organization.OrganizationReportBrandingUpdateRequest;
import com.halo.lims.dto.organization.OrganizationResponse;
import com.halo.lims.service.ImageService;
import com.halo.lims.model.Organization;
import com.halo.lims.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final ImageService imageService;

    public OrganizationService(OrganizationRepository organizationRepository,
                               ImageService imageService) {
        this.organizationRepository = organizationRepository;
        this.imageService = imageService;
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
                .gstin(request.getGstin())
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

    @Transactional
    public OrganizationResponse updateOrganizationReportBranding(Integer id, OrganizationReportBrandingUpdateRequest request) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + id));

        if (request.getReportHeaderImage() != null) {
            String headerImage = request.getReportHeaderImage().trim();
            if (headerImage.isBlank()) {
                organization.setHeaderImageAssetId(null);
                organization.setReportHeaderImage(null);
            } else {
                Integer assetId = imageService.upsertImageAsset(headerImage, "ORG_HEADER", "organization", id, null);
                organization.setHeaderImageAssetId(assetId);
                organization.setReportHeaderImage(assetId == null ? headerImage : null);
            }
        }
        if (request.getReportFooterImage() != null) {
            String footerImage = request.getReportFooterImage().trim();
            if (footerImage.isBlank()) {
                organization.setFooterImageAssetId(null);
                organization.setReportFooterImage(null);
            } else {
                Integer assetId = imageService.upsertImageAsset(footerImage, "ORG_FOOTER", "organization", id, null);
                organization.setFooterImageAssetId(assetId);
                organization.setReportFooterImage(assetId == null ? footerImage : null);
            }
        }
        if (request.getReportHeaderMarginMm() != null) {
            organization.setReportHeaderMarginMm(request.getReportHeaderMarginMm());
        }
        if (request.getReportFooterMarginMm() != null) {
            organization.setReportFooterMarginMm(request.getReportFooterMarginMm());
        }
        if (request.getReportHeaderHeightMm() != null) {
            organization.setReportHeaderHeightMm(request.getReportHeaderHeightMm());
        }
        if (request.getReportFooterHeightMm() != null) {
            organization.setReportFooterHeightMm(request.getReportFooterHeightMm());
        }
        if (request.getGstin() != null) {
            String gstin = request.getGstin().trim().toUpperCase();
            organization.setGstin(gstin.isBlank() ? null : gstin);
        }

        Organization saved = organizationRepository.save(organization);
        return mapToOrganizationResponse(saved);
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
        response.setGstin(organization.getGstin());
        response.setLocalIdentifierValue(organization.getLocalIdentifierValue());
        response.setReportHeaderImage(imageService.resolveImageUrl(organization.getHeaderImageAssetId(), organization.getReportHeaderImage()));
        response.setReportFooterImage(imageService.resolveImageUrl(organization.getFooterImageAssetId(), organization.getReportFooterImage()));
        response.setReportHeaderMarginMm(organization.getReportHeaderMarginMm());
        response.setReportFooterMarginMm(organization.getReportFooterMarginMm());
        response.setReportHeaderHeightMm(organization.getReportHeaderHeightMm());
        response.setReportFooterHeightMm(organization.getReportFooterHeightMm());
        response.setCreatedAt(organization.getCreatedAt());
        response.setUpdatedAt(organization.getUpdatedAt());
        return response;
    }
}

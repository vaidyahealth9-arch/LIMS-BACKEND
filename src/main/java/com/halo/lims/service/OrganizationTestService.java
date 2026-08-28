package com.halo.lims.service;

import com.halo.lims.dto.organization.test.OrganizationTestRequest;
import com.halo.lims.dto.organization.test.OrganizationTestResponse;
import com.halo.lims.model.Organization;
import com.halo.lims.model.OrganizationTest;
import com.halo.lims.model.OrganizationTestAnalyte;
import com.halo.lims.model.SpecimenType;
import com.halo.lims.model.Test;
import com.halo.lims.model.TestAnalyte;
import com.halo.lims.repository.OrganizationRepository;
import com.halo.lims.repository.OrganizationTestAnalyteRepository;
import com.halo.lims.repository.OrganizationTestRepository;
import com.halo.lims.repository.SpecimenTypeRepository;
import com.halo.lims.repository.TestAnalyteRepository;
import com.halo.lims.repository.TestRepository;
import com.halo.lims.security.SecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizationTestService {

    private final OrganizationTestRepository organizationTestRepository;
    private final OrganizationRepository organizationRepository;
    private final TestRepository testRepository;
    private final SpecimenTypeRepository specimenTypeRepository;
    private final SecurityService securityService;

    public OrganizationTestService(OrganizationTestRepository organizationTestRepository,
                                   OrganizationRepository organizationRepository,
                                   TestRepository testRepository,
                                   SpecimenTypeRepository specimenTypeRepository,
                                   SecurityService securityService) {
        this.organizationTestRepository = organizationTestRepository;
        this.organizationRepository = organizationRepository;
        this.testRepository = testRepository;
        this.specimenTypeRepository = specimenTypeRepository;
        this.securityService = securityService;
    }

    /**
     * Adds a test to an organization's catalog or updates its enablement/price/specimen.
     * @param organizationId The ID of the organization.
     * @param request The DTO containing test ID, enabled status, and price.
     * @return The updated/created OrganizationTestResponse.
     */
    @Transactional
    public OrganizationTestResponse addOrUpdateOrganizationTest(Integer organizationId, OrganizationTestRequest request) {
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to manage tests for organization ID: " + organizationId);
        }

        OrganizationTest organizationTest = getOrCreateOrganizationTest(organizationId, request.getTestId());
        organizationTest.setIsEnabled(request.getIsEnabled());
        organizationTest.setPrice(request.getPrice());
        organizationTest.setDefaultNumberOfSpecimens(request.getDefaultNumberOfSpecimens());

        if (request.getSpecimenTypeId() != null) {
            SpecimenType specimenType = specimenTypeRepository.findById(request.getSpecimenTypeId())
                    .orElseThrow(() -> new RuntimeException("Specimen type not found with ID: " + request.getSpecimenTypeId()));
            organizationTest.setSpecimenType(specimenType);
        } else {
            organizationTest.setSpecimenType(null);
        }

        OrganizationTest savedOrgTest = organizationTestRepository.save(organizationTest);
        return mapToOrganizationTestResponse(savedOrgTest);
    }

    /**
     * Retrieves an organization's specific test catalog entry.
     * @param organizationId The ID of the organization.
     * @param testId The ID of the test.
     * @return The OrganizationTestResponse.
     */
    @Transactional(readOnly = true)
    public OrganizationTestResponse getOrganizationTest(Integer organizationId, Integer testId) {
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to view tests for organization ID: " + organizationId);
        }

        OrganizationTest organizationTest = organizationTestRepository.findByOrganization_IdAndTest_Id(organizationId, testId)
                .orElseThrow(() -> new RuntimeException("Test ID " + testId + " not found in organization " + organizationId + " catalog."));
        return mapToOrganizationTestResponse(organizationTest);
    }

    /**
     * Retrieves all tests (enabled or disabled) for a specific organization.
     * @param organizationId The ID of the organization.
     * @return A list of OrganizationTestResponses.
     */
    @Transactional(readOnly = true)
    public List<OrganizationTestResponse> getAllOrganizationTests(Integer organizationId) {
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to view tests for organization ID: " + organizationId);
        }

        return organizationTestRepository.findByOrganization_Id(organizationId).stream()
                .map(this::mapToOrganizationTestResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<OrganizationTestResponse> bulkUpdateOrganizationTestPrices(Integer organizationId, List<Integer> testIds, BigDecimal price) {
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to update tests for organization ID: " + organizationId);
        }

        if (testIds == null || testIds.isEmpty()) {
            throw new IllegalArgumentException("At least one test must be selected for bulk price update.");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be a non-negative value.");
        }

        List<OrganizationTest> organizationTests = organizationTestRepository.findByOrganization_IdAndTest_IdIn(organizationId, testIds);
        if (organizationTests.size() != testIds.size()) {
            throw new RuntimeException("One or more selected tests were not found in the organization catalog.");
        }

        organizationTests.forEach(organizationTest -> organizationTest.setPrice(price));
        List<OrganizationTest> saved = organizationTestRepository.saveAll(organizationTests);

        return saved.stream()
                .map(this::mapToOrganizationTestResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves only the enabled tests for a specific organization (the actual catalog).
     * @param organizationId The ID of the organization.
     * @return A list of OrganizationTestResponses for enabled tests.
     */
    @Transactional(readOnly = true)
    public List<OrganizationTestResponse> getEnabledOrganizationTests(Integer organizationId) {
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to view enabled tests for organization ID: " + organizationId);
        }

        return organizationTestRepository.findByOrganization_IdAndIsEnabled(organizationId, true).stream()
                .map(this::mapToOrganizationTestResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrganizationTest getOrCreateOrganizationTest(Integer organizationId, Integer testId) {
        return organizationTestRepository.findByOrganization_IdAndTest_Id(organizationId, testId)
                .orElseGet(() -> {
                    Organization organization = organizationRepository.findById(organizationId)
                            .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));
                    Test test = testRepository.findById(testId)
                            .orElseThrow(() -> new RuntimeException("Test not found with ID: " + testId));
                    OrganizationTest newOrgTest = OrganizationTest.builder()
                            .organization(organization)
                            .test(test)
                            .isEnabled(true)
                            .price(null) // or a default price
                            .build();
                    return organizationTestRepository.save(newOrgTest);
                });
    }

    private OrganizationTestResponse mapToOrganizationTestResponse(OrganizationTest organizationTest) {
        OrganizationTestResponse response = new OrganizationTestResponse();
        response.setOrganizationId(organizationTest.getOrganization().getId());
        response.setOrganizationName(organizationTest.getOrganization().getOrganizationName());
        response.setTestId(organizationTest.getTest().getId());
        response.setTestLocalCode(organizationTest.getTest().getLocalCode());
        response.setTestName(organizationTest.getTest().getTestName());
        response.setIsEnabled(organizationTest.getIsEnabled());
        response.setPrice(organizationTest.getPrice());
        response.setDefaultNumberOfSpecimens(organizationTest.getDefaultNumberOfSpecimens());
        if (organizationTest.getSpecimenType() != null) {
            response.setSpecimenTypeId(organizationTest.getSpecimenType().getId());
            response.setSpecimenTypeName(organizationTest.getSpecimenType().getName());
        }
        response.setCreatedAt(organizationTest.getCreatedAt());
        response.setUpdatedAt(organizationTest.getUpdatedAt());
        return response;
    }
}

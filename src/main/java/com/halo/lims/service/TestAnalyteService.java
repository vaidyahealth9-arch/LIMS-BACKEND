package com.halo.lims.service;

import com.halo.lims.dto.test.TestAnalyteCreateRequest;
import com.halo.lims.dto.test.TestAnalyteResponse;
import com.halo.lims.dto.test.TestAnalyteUpdateRequest;
import com.halo.lims.model.Test;
import com.halo.lims.model.TestAnalyte;
import com.halo.lims.model.Unit;
import com.halo.lims.repository.TestAnalyteRepository;
import com.halo.lims.repository.TestRepository;
import com.halo.lims.repository.UnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.halo.lims.model.Organization;
import com.halo.lims.repository.OrganizationRepository;

@Service
public class TestAnalyteService {

    private final TestAnalyteRepository testAnalyteRepository;
    private final TestRepository testRepository;
    private final UnitRepository unitRepository;
    private final OrganizationRepository organizationRepository;

    public TestAnalyteService(TestAnalyteRepository testAnalyteRepository, TestRepository testRepository, UnitRepository unitRepository, OrganizationRepository organizationRepository) {
        this.testAnalyteRepository = testAnalyteRepository;
        this.testRepository = testRepository;
        this.unitRepository = unitRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public TestAnalyteResponse createTestAnalyte(TestAnalyteCreateRequest request) {
        if (testAnalyteRepository.existsByAnalyteCode(request.getAnalyteCode())) {
            throw new IllegalArgumentException("Test Analyte with code " + request.getAnalyteCode() + " already exists.");
        }

        Test parentTest = testRepository.findById(request.getParentTestId())
                .orElseThrow(() -> new RuntimeException("Parent Test not found with ID: " + request.getParentTestId()));

        Unit unit = null;
        if (request.getUnitId() != null) {
            unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new RuntimeException("Unit not found with ID: " + request.getUnitId()));
        }

        Organization org = null;
        if (request.getOrganizationId() != null) {
            org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + request.getOrganizationId()));
        }

        TestAnalyte testAnalyte = TestAnalyte.builder()
                .organization(org)
                .analyteCode(request.getAnalyteCode())
                .analyteName(request.getAnalyteName())
                .parentTest(parentTest)
                .loincCode(request.getLoincCode())
                .loincSystem(request.getLoincSystem() != null ? request.getLoincSystem() : "http://loinc.org")
                .unit(unit)
                .resultType(request.getResultType())
                .decimalPlaces(request.getDecimalPlaces())
                .biologicalRefInterval(request.getBiologicalRefInterval())
                .isDerived(request.getIsDerived())
                .formula(request.getFormula())
                .build();

        TestAnalyte savedAnalyte = testAnalyteRepository.save(testAnalyte);
        return mapToTestAnalyteResponse(savedAnalyte);
    }

    @Transactional
    public TestAnalyteResponse updateTestAnalyte(Integer id, TestAnalyteUpdateRequest request) {
        TestAnalyte testAnalyte = testAnalyteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test Analyte not found with ID: " + id));

        if (request.getAnalyteName() != null) testAnalyte.setAnalyteName(request.getAnalyteName());
        if (request.getLoincCode() != null) testAnalyte.setLoincCode(request.getLoincCode());
        if (request.getLoincSystem() != null) testAnalyte.setLoincSystem(request.getLoincSystem());
        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new RuntimeException("Unit not found with ID: " + request.getUnitId()));
            testAnalyte.setUnit(unit);
        }
        if (request.getResultType() != null) testAnalyte.setResultType(request.getResultType());
        if (request.getDecimalPlaces() != null) testAnalyte.setDecimalPlaces(request.getDecimalPlaces());
        if (request.getBiologicalRefInterval() != null) testAnalyte.setBiologicalRefInterval(request.getBiologicalRefInterval());
        if (request.getIsDerived() != null) testAnalyte.setIsDerived(request.getIsDerived());
        if (request.getFormula() != null) testAnalyte.setFormula(request.getFormula());

        TestAnalyte updatedAnalyte = testAnalyteRepository.save(testAnalyte);
        return mapToTestAnalyteResponse(updatedAnalyte);
    }

    @Transactional(readOnly = true)
    public TestAnalyteResponse getTestAnalyteById(Integer id) {
        TestAnalyte testAnalyte = testAnalyteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test Analyte not found with ID: " + id));
        return mapToTestAnalyteResponse(testAnalyte);
    }

    @Transactional(readOnly = true)
    public List<TestAnalyteResponse> getAllTestAnalytes() {
        return testAnalyteRepository.findAll().stream()
                .map(this::mapToTestAnalyteResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TestAnalyteResponse> getTestAnalytesByParentTest(Integer parentTestId) {
        testRepository.findById(parentTestId) // Validate parent test exists
                .orElseThrow(() -> new RuntimeException("Parent Test not found with ID: " + parentTestId));
        return testAnalyteRepository.findByParentTestId(parentTestId).stream()
                .map(this::mapToTestAnalyteResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTestAnalyte(Integer id) {
        if (!testAnalyteRepository.existsById(id)) {
            throw new RuntimeException("Test Analyte not found with ID: " + id);
        }
        testAnalyteRepository.deleteById(id);
    }

    private TestAnalyteResponse mapToTestAnalyteResponse(TestAnalyte testAnalyte) {
        TestAnalyteResponse response = new TestAnalyteResponse();
        response.setId(testAnalyte.getId());
        response.setAnalyteCode(testAnalyte.getAnalyteCode());
        response.setAnalyteName(testAnalyte.getAnalyteName());
        response.setParentTestId(testAnalyte.getParentTest().getId());
        response.setParentTestLocalCode(testAnalyte.getParentTest().getLocalCode());
        response.setParentTestName(testAnalyte.getParentTest().getTestName());
        response.setLoincCode(testAnalyte.getLoincCode());
        response.setLoincSystem(testAnalyte.getLoincSystem());
        if (testAnalyte.getUnit() != null) {
            response.setUnitId(testAnalyte.getUnit().getId());
            response.setUnitName(testAnalyte.getUnit().getName());
        }
        response.setResultType(testAnalyte.getResultType());
        response.setDecimalPlaces(testAnalyte.getDecimalPlaces());
        response.setBiologicalRefInterval(testAnalyte.getBiologicalRefInterval());
        response.setIsDerived(testAnalyte.getIsDerived());
        response.setFormula(testAnalyte.getFormula());
        if (testAnalyte.getOrganization() != null) {
            response.setOrganizationId(testAnalyte.getOrganization().getId());
        }
        response.setCreatedAt(testAnalyte.getCreatedAt());
        response.setUpdatedAt(testAnalyte.getUpdatedAt());
        return response;
    }
}

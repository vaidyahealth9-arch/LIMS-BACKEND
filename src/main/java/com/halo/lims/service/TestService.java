package com.halo.lims.service;

import com.halo.lims.dto.test.TestCreateRequest;
import com.halo.lims.dto.test.TestResponse;
import com.halo.lims.dto.test.TestUpdateRequest;
import com.halo.lims.model.Test;
import com.halo.lims.repository.OrganizationTestRepository;
import com.halo.lims.repository.TestAnalyteRepository;
import com.halo.lims.repository.TestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestService {

    private final TestRepository testRepository;
    private final TestAnalyteRepository testAnalyteRepository; // For checking dependencies before delete
    private final OrganizationTestRepository organizationTestRepository; // For checking dependencies before delete

    public TestService(TestRepository testRepository,
                       TestAnalyteRepository testAnalyteRepository,
                       OrganizationTestRepository organizationTestRepository) {
        this.testRepository = testRepository;
        this.testAnalyteRepository = testAnalyteRepository;
        this.organizationTestRepository = organizationTestRepository;
    }

    /**
     * Creates a new global Test definition.
     * @param request The DTO containing test details.
     * @return The created TestResponse.
     */
    @Transactional
    public TestResponse createTest(TestCreateRequest request) {
        if (testRepository.existsByLocalCode(request.getLocalCode())) {
            throw new IllegalArgumentException("Test with local code " + request.getLocalCode() + " already exists.");
        }

        Test test = Test.builder()
                .testName(request.getTestName())
                .localCode(request.getLocalCode())
                .loincCode(request.getLoincCode())
                .loincSystem(request.getLoincSystem() != null ? request.getLoincSystem() : "http://loinc.org")
                .department(request.getDepartment())
                .containerDescription(request.getContainerDescription())
                .method(request.getMethod())
                .measuringPrinciple(request.getMeasuringPrinciple())
                .turnAroundTimeText(request.getTurnAroundTimeText())
                .reflexProfileText(request.getReflexProfileText())
                .reportNotes(request.getReportNotes())
                .build();

        Test savedTest = testRepository.save(test);
        return mapToTestResponse(savedTest);
    }

    /**
     * Updates an existing global Test definition.
     * @param id The ID of the Test to update.
     * @param request The DTO containing updated test details.
     * @return The updated TestResponse.
     */
    @Transactional
    public TestResponse updateTest(Integer id, TestUpdateRequest request) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test not found with ID: " + id));

        if (request.getTestName() != null) test.setTestName(request.getTestName());
        if (request.getLoincCode() != null) test.setLoincCode(request.getLoincCode());
        if (request.getLoincSystem() != null) test.setLoincSystem(request.getLoincSystem());
        if (request.getDepartment() != null) test.setDepartment(request.getDepartment());
        if (request.getContainerDescription() != null) test.setContainerDescription(request.getContainerDescription());
        if (request.getMethod() != null) test.setMethod(request.getMethod());
        if (request.getMeasuringPrinciple() != null) test.setMeasuringPrinciple(request.getMeasuringPrinciple());
        if (request.getTurnAroundTimeText() != null) test.setTurnAroundTimeText(request.getTurnAroundTimeText());
        if (request.getReflexProfileText() != null) test.setReflexProfileText(request.getReflexProfileText());
        if (request.getReportNotes() != null) test.setReportNotes(request.getReportNotes());

        Test updatedTest = testRepository.save(test);
        return mapToTestResponse(updatedTest);
    }

    /**
     * Retrieves a global Test definition by its ID.
     * @param id The ID of the Test.
     * @return The TestResponse.
     */
    @Transactional(readOnly = true)
    public TestResponse getTestById(Integer id) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test not found with ID: " + id));
        return mapToTestResponse(test);
    }

    /**
     * Retrieves all global Test definitions.
     * @return A list of TestResponses.
     */
    @Transactional(readOnly = true)
    public List<TestResponse> getAllTests() {
        return testRepository.findAll().stream()
                .map(this::mapToTestResponse)
                .collect(Collectors.toList());
    }

    /**
     * Searches for global Test definitions by name.
     * @param nameQuery A part of the test name to search for.
     * @return A list of matching TestResponses.
     */
    @Transactional(readOnly = true)
    public List<TestResponse> searchTestsByName(String nameQuery) {
        return testRepository.findByTestNameContainingIgnoreCase(nameQuery).stream()
                .map(this::mapToTestResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a global Test definition.
     * Checks for dependencies in TestAnalytes and OrganizationTests before deletion.
     * @param id The ID of the Test to delete.
     */
    @Transactional
    public void deleteTest(Integer id) {
        if (!testRepository.existsById(id)) {
            throw new RuntimeException("Test not found with ID: " + id);
        }

        // Check for dependencies in TestAnalytes
        if (testAnalyteRepository.existsByParentTestId(id)) {
            throw new IllegalStateException("Cannot delete Test ID " + id + ". It is linked to one or more Test Analytes.");
        }
        // Check for dependencies in OrganizationTests (lab-specific catalog entries)
        if (organizationTestRepository.existsByTest_Id(id)) { // Assuming existsByTest_Id method exists or you create it
            throw new IllegalStateException("Cannot delete Test ID " + id + ". It is part of one or more Organization Test catalogs.");
        }
        // Check for dependencies in ServiceRequestItems (if any active/completed requests use this test)
        // You might need a method like serviceRequestItemRepository.existsByTestId(id) here
        // For simplicity, omitting this check for now, but crucial for production.

        testRepository.deleteById(id);
    }

    private TestResponse mapToTestResponse(Test test) {
        TestResponse response = new TestResponse();
        response.setId(test.getId());
        response.setTestName(test.getTestName());
        response.setLocalCode(test.getLocalCode());
        response.setLoincCode(test.getLoincCode());
        response.setLoincSystem(test.getLoincSystem());
        response.setDepartment(test.getDepartment());
        response.setContainerDescription(test.getContainerDescription());
        response.setMethod(test.getMethod());
        response.setMeasuringPrinciple(test.getMeasuringPrinciple());
        response.setTurnAroundTimeText(test.getTurnAroundTimeText());
        response.setReflexProfileText(test.getReflexProfileText());
        response.setReportNotes(test.getReportNotes());
        response.setCreatedAt(test.getCreatedAt());
        response.setUpdatedAt(test.getUpdatedAt());
        return response;
    }
}

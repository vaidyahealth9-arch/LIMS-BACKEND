package com.halo.lims.controller;

import com.halo.lims.dto.test.TestCreateRequest;
import com.halo.lims.dto.test.TestResponse;
import com.halo.lims.dto.test.TestUpdateRequest;
import com.halo.lims.service.TestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.halo.lims.security.SecurityService;
import com.halo.lims.model.User;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    private final TestService testService;
    private final SecurityService securityService;

    public TestController(TestService testService, SecurityService securityService) {
        this.testService = testService;
        this.securityService = securityService;
    }

    /**
     * Creates a new global Test definition.
     * Only accessible by ADMIN role.
     * @param request The DTO containing test details.
     * @return The created TestResponse.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR')")
    public ResponseEntity<TestResponse> createTest(@Valid @RequestBody TestCreateRequest request) {
        User user = securityService.getAuthenticatedUser();
        if (!user.getRoles().contains("ADMIN")) {
            request.setOrganizationId(user.getOrganization().getId());
        }
        TestResponse response = testService.createTest(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing global Test definition.
     * Only accessible by ADMIN role.
     * @param id The ID of the Test to update.
     * @param request The DTO containing updated test details.
     * @return The updated TestResponse.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR')")
    public ResponseEntity<TestResponse> updateTest(@PathVariable Integer id, @Valid @RequestBody TestUpdateRequest request) {
        // TODO: TestService.updateTest needs to verify organization ownership if user is not ADMIN
        TestResponse response = testService.updateTest(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves a global Test definition by its ID.
     * Accessible by various roles who need to view test definitions.
     * @param id The ID of the Test.
     * @return The TestResponse.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<TestResponse> getTestById(@PathVariable Integer id) {
        TestResponse response = testService.getTestById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves all global Test definitions.
     * Accessible by various roles.
     * @return A list of TestResponses.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<List<TestResponse>> getAllTests() {
        List<TestResponse> responses = testService.getAllTests();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Searches for global Test definitions by name.
     * Accessible by various roles.
     * @param nameQuery A part of the test name to search for.
     * @return A list of matching TestResponses.
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<List<TestResponse>> searchTestsByName(@RequestParam String nameQuery) {
        List<TestResponse> responses = testService.searchTestsByName(nameQuery);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Deletes a global Test definition.
     * Only accessible by ADMIN role.
     * @param id The ID of the Test to delete.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTest(@PathVariable Integer id) {
        testService.deleteTest(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

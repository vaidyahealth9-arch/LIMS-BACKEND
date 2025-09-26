package com.halo.lims.controller;

import com.halo.lims.dto.specimenType.SpecimenTypeCreateRequest;
import com.halo.lims.dto.specimenType.SpecimenTypeResponse;
import com.halo.lims.dto.specimenType.SpecimenTypeUpdateRequest;
import com.halo.lims.service.SpecimenTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specimen-types")
public class SpecimenTypeController {

    private final SpecimenTypeService specimenTypeService;

    public SpecimenTypeController(SpecimenTypeService specimenTypeService) {
        this.specimenTypeService = specimenTypeService;
    }

    /**
     * Creates a new specimen type.
     * Accessible by ADMIN role.
     * @param request The DTO containing specimen type details.
     * @return The created SpecimenTypeResponse.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecimenTypeResponse> createSpecimenType(@Valid @RequestBody SpecimenTypeCreateRequest request) {
        SpecimenTypeResponse response = specimenTypeService.createSpecimenType(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing specimen type.
     * Accessible by ADMIN role.
     * @param id The ID of the specimen type to update.
     * @param request The DTO containing updated specimen type details.
     * @return The updated SpecimenTypeResponse.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecimenTypeResponse> updateSpecimenType(@PathVariable Integer id, @Valid @RequestBody SpecimenTypeUpdateRequest request) {
        SpecimenTypeResponse response = specimenTypeService.updateSpecimenType(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves a specimen type by its ID.
     * Accessible by various roles who need to view specimen types.
     * @param id The ID of the specimen type.
     * @return The SpecimenTypeResponse.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<SpecimenTypeResponse> getSpecimenTypeById(@PathVariable Integer id) {
        SpecimenTypeResponse response = specimenTypeService.getSpecimenTypeById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves all specimen types.
     * Accessible by various roles.
     * @return A list of SpecimenTypeResponses.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<List<SpecimenTypeResponse>> getAllSpecimenTypes() {
        List<SpecimenTypeResponse> responses = specimenTypeService.getAllSpecimenTypes();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Deletes a specimen type by its ID.
     * Accessible by ADMIN role.
     * @param id The ID of the specimen type to delete.
     * @return No content response.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSpecimenType(@PathVariable Integer id) {
        specimenTypeService.deleteSpecimenType(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

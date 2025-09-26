package com.halo.lims.controller;

import com.halo.lims.dto.unit.UnitCreateRequest;
import com.halo.lims.dto.unit.UnitResponse;
import com.halo.lims.dto.unit.UnitUpdateRequest;
import com.halo.lims.service.UnitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/units")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    /**
     * Creates a new measurement unit.
     * Accessible by ADMIN role.
     * @param request The DTO containing unit details.
     * @return The created UnitResponse.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnitResponse> createUnit(@Valid @RequestBody UnitCreateRequest request) {
        UnitResponse response = unitService.createUnit(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing measurement unit.
     * Accessible by ADMIN role.
     * @param id The ID of the unit to update.
     * @param request The DTO containing updated unit details.
     * @return The updated UnitResponse.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnitResponse> updateUnit(@PathVariable Integer id, @Valid @RequestBody UnitUpdateRequest request) {
        UnitResponse response = unitService.updateUnit(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves a unit by its ID.
     * Accessible by various roles who need to view units.
     * @param id The ID of the unit.
     * @return The UnitResponse.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<UnitResponse> getUnitById(@PathVariable Integer id) {
        UnitResponse response = unitService.getUnitById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves all units.
     * Accessible by various roles.
     * @return A list of UnitResponses.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<List<UnitResponse>> getAllUnits() {
        List<UnitResponse> responses = unitService.getAllUnits();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Deletes a unit by its ID.
     * Accessible by ADMIN role.
     * @param id The ID of the unit to delete.
     * @return No content response.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUnit(@PathVariable Integer id) {
        unitService.deleteUnit(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

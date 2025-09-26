package com.halo.lims.service;

import com.halo.lims.dto.unit.UnitCreateRequest;
import com.halo.lims.dto.unit.UnitResponse;
import com.halo.lims.dto.unit.UnitUpdateRequest;
import com.halo.lims.model.Unit;
import com.halo.lims.repository.UnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UnitService {

    private final UnitRepository unitRepository;

    public UnitService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    /**
     * Creates a new measurement unit.
     * @param request The DTO containing unit details.
     * @return The created UnitResponse.
     */
    @Transactional
    public UnitResponse createUnit(UnitCreateRequest request) {
        if (unitRepository.existsByUcumCode(request.getUcumCode())) {
            throw new IllegalArgumentException("Unit with UCUM code " + request.getUcumCode() + " already exists.");
        }
        if (unitRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Unit with name " + request.getName() + " already exists.");
        }

        Unit unit = Unit.builder()
                .name(request.getName())
                .ucumCode(request.getUcumCode())
                .description(request.getDescription())
                .build();

        Unit savedUnit = unitRepository.save(unit);
        return mapToUnitResponse(savedUnit);
    }

    /**
     * Updates an existing measurement unit.
     * @param id The ID of the unit to update.
     * @param request The DTO containing updated unit details.
     * @return The updated UnitResponse.
     */
    @Transactional
    public UnitResponse updateUnit(Integer id, UnitUpdateRequest request) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unit not found with ID: " + id));

        if (request.getName() != null) {
            if (unitRepository.findByName(request.getName()).isPresent() && !unitRepository.findByName(request.getName()).get().getId().equals(id)) {
                throw new IllegalArgumentException("Unit with name " + request.getName() + " already exists.");
            }
            unit.setName(request.getName());
        }
        if (request.getDescription() != null) unit.setDescription(request.getDescription());

        Unit updatedUnit = unitRepository.save(unit);
        return mapToUnitResponse(updatedUnit);
    }

    /**
     * Retrieves a unit by its ID.
     * @param id The ID of the unit.
     * @return The UnitResponse.
     */
    @Transactional(readOnly = true)
    public UnitResponse getUnitById(Integer id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unit not found with ID: " + id));
        return mapToUnitResponse(unit);
    }

    /**
     * Retrieves all units.
     * @return A list of UnitResponses.
     */
    @Transactional(readOnly = true)
    public List<UnitResponse> getAllUnits() {
        return unitRepository.findAll().stream()
                .map(this::mapToUnitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a unit by its ID.
     * @param id The ID of the unit to delete.
     */
    @Transactional
    public void deleteUnit(Integer id) {
        if (!unitRepository.existsById(id)) {
            throw new RuntimeException("Unit not found with ID: " + id);
        }
        // TODO: Add dependency checks before deletion (e.g., if used by TestAnalytes, ReferenceRanges)
        unitRepository.deleteById(id);
    }

    private UnitResponse mapToUnitResponse(Unit unit) {
        UnitResponse response = new UnitResponse();
        response.setId(unit.getId());
        response.setName(unit.getName());
        response.setUcumCode(unit.getUcumCode());
        response.setDescription(unit.getDescription());
        response.setCreatedAt(unit.getCreatedAt());
        response.setUpdatedAt(unit.getUpdatedAt());
        return response;
    }
}

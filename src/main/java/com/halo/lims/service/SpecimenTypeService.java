package com.halo.lims.service;

import com.halo.lims.dto.specimenType.SpecimenTypeCreateRequest;
import com.halo.lims.dto.specimenType.SpecimenTypeResponse;
import com.halo.lims.dto.specimenType.SpecimenTypeUpdateRequest;
import com.halo.lims.model.SpecimenType;
import com.halo.lims.repository.SpecimenTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpecimenTypeService {

    private final SpecimenTypeRepository specimenTypeRepository;

    public SpecimenTypeService(SpecimenTypeRepository specimenTypeRepository) {
        this.specimenTypeRepository = specimenTypeRepository;
    }

    /**
     * Creates a new specimen type.
     * @param request The DTO containing specimen type details.
     * @return The created SpecimenTypeResponse.
     */
    @Transactional
    public SpecimenTypeResponse createSpecimenType(SpecimenTypeCreateRequest request) {
        if (request.getSnomedCode() != null && specimenTypeRepository.existsBySnomedCode(request.getSnomedCode())) {
            throw new IllegalArgumentException("Specimen type with SNOMED code " + request.getSnomedCode() + " already exists.");
        }
        if (specimenTypeRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Specimen type with name " + request.getName() + " already exists.");
        }

        SpecimenType specimenType = SpecimenType.builder()
                .name(request.getName())
                .snomedCode(request.getSnomedCode())
                .snomedSystem(request.getSnomedSystem() != null ? request.getSnomedSystem() : "http://snomed.info/sct")
                .description(request.getDescription())
                .build();

        SpecimenType savedSpecimenType = specimenTypeRepository.save(specimenType);
        return mapToSpecimenTypeResponse(savedSpecimenType);
    }

    /**
     * Updates an existing specimen type.
     * @param id The ID of the specimen type to update.
     * @param request The DTO containing updated specimen type details.
     * @return The updated SpecimenTypeResponse.
     */
    @Transactional
    public SpecimenTypeResponse updateSpecimenType(Integer id, SpecimenTypeUpdateRequest request) {
        SpecimenType specimenType = specimenTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specimen Type not found with ID: " + id));

        if (request.getName() != null) {
            if (specimenTypeRepository.findByName(request.getName()).isPresent() && !specimenTypeRepository.findByName(request.getName()).get().getId().equals(id)) {
                throw new IllegalArgumentException("Specimen type with name " + request.getName() + " already exists.");
            }
            specimenType.setName(request.getName());
        }
        if (request.getSnomedCode() != null) {
            if (specimenTypeRepository.findBySnomedCode(request.getSnomedCode()).isPresent() && !specimenTypeRepository.findBySnomedCode(request.getSnomedCode()).get().getId().equals(id)) {
                throw new IllegalArgumentException("Specimen type with SNOMED code " + request.getSnomedCode() + " already exists.");
            }
            specimenType.setSnomedCode(request.getSnomedCode());
        }
        if (request.getSnomedSystem() != null) specimenType.setSnomedSystem(request.getSnomedSystem());
        if (request.getDescription() != null) specimenType.setDescription(request.getDescription());

        SpecimenType updatedSpecimenType = specimenTypeRepository.save(specimenType);
        return mapToSpecimenTypeResponse(updatedSpecimenType);
    }

    /**
     * Retrieves a specimen type by its ID.
     * @param id The ID of the specimen type.
     * @return The SpecimenTypeResponse.
     */
    @Transactional(readOnly = true)
    public SpecimenTypeResponse getSpecimenTypeById(Integer id) {
        SpecimenType specimenType = specimenTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specimen Type not found with ID: " + id));
        return mapToSpecimenTypeResponse(specimenType);
    }

    /**
     * Retrieves all specimen types.
     * @return A list of SpecimenTypeResponses.
     */
    @Transactional(readOnly = true)
    public List<SpecimenTypeResponse> getAllSpecimenTypes() {
        return specimenTypeRepository.findAll().stream()
                .map(this::mapToSpecimenTypeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a specimen type by its ID.
     * @param id The ID of the specimen type to delete.
     */
    @Transactional
    public void deleteSpecimenType(Integer id) {
        if (!specimenTypeRepository.existsById(id)) {
            throw new RuntimeException("Specimen Type not found with ID: " + id);
        }
        // TODO: Add dependency checks before deletion (e.g., if used by Tests, Specimens)
        specimenTypeRepository.deleteById(id);
    }

    private SpecimenTypeResponse mapToSpecimenTypeResponse(SpecimenType specimenType) {
        SpecimenTypeResponse response = new SpecimenTypeResponse();
        response.setId(specimenType.getId());
        response.setName(specimenType.getName());
        response.setSnomedCode(specimenType.getSnomedCode());
        response.setSnomedSystem(specimenType.getSnomedSystem());
        response.setDescription(specimenType.getDescription());
        response.setCreatedAt(specimenType.getCreatedAt());
        response.setUpdatedAt(specimenType.getUpdatedAt());
        return response;
    }
}

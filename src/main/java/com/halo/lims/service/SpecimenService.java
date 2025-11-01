package com.halo.lims.service;

import com.halo.lims.dto.specimen.SpecimenCreateRequest;
import com.halo.lims.dto.specimen.SpecimenResponse;
import com.halo.lims.dto.specimen.SpecimenUpdateRequest;
import com.halo.lims.model.ServiceRequest;
import com.halo.lims.model.Specimen;
import com.halo.lims.model.SpecimenType;
import com.halo.lims.model.Unit;
import com.halo.lims.repository.*;
import com.halo.lims.security.SecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SpecimenService {

    private final SpecimenRepository specimenRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final PatientRepository patientRepository;
    private final SpecimenTypeRepository specimenTypeRepository;
    private final UnitRepository unitRepository;
    private final SecurityService securityService;
    private final BarcodeService barcodeService;

    public SpecimenService(SpecimenRepository specimenRepository,
                           ServiceRequestRepository serviceRequestRepository,
                           PatientRepository patientRepository,
                           SpecimenTypeRepository specimenTypeRepository,
                           UnitRepository unitRepository,
                           SecurityService securityService, BarcodeService barcodeService) {
        this.specimenRepository = specimenRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.patientRepository = patientRepository;
        this.specimenTypeRepository = specimenTypeRepository;
        this.unitRepository = unitRepository;
        this.securityService = securityService;
        this.barcodeService = barcodeService;
    }

    @Transactional
    public SpecimenResponse createSpecimen(SpecimenCreateRequest request) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(request.getServiceRequestId())
                .orElseThrow(() -> new RuntimeException("Service Request not found with ID: " + request.getServiceRequestId()));
        SpecimenType specimenType = specimenTypeRepository.findById(request.getSpecimenTypeId())
                .orElseThrow(() -> new RuntimeException("Specimen Type not found with ID: " + request.getSpecimenTypeId()));
        Unit quantityUnit = null;
        if (request.getQuantityUnitId() != null) {
            quantityUnit = unitRepository.findById(request.getQuantityUnitId())
                    .orElseThrow(() -> new RuntimeException("Quantity Unit not found with ID: " + request.getQuantityUnitId()));
        }

        // --- Multi-tenancy check ---
        Integer organizationId = serviceRequest.getPatient().getOrganization().getId();
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to create specimens for service requests in organization ID: " + organizationId);
        }
        // --- End multi-tenancy check ---

        String localSpecimenId = generateLocalSpecimenId();
        String barcodeImage = "";
        try {
            barcodeImage = barcodeService.generateBarcodeImageBase64(localSpecimenId);
        } catch (Exception e) {
            throw new RuntimeException("Error generating barcode", e);
        }

        Specimen specimen = Specimen.builder()
                .serviceRequest(serviceRequest)
                .patient(serviceRequest.getPatient()) // Patient comes from ServiceRequest for consistency
                .specimenType(specimenType)
                .collectionDate(request.getCollectionDate())
                .receivedDate(request.getReceivedDate())
                .status(request.getStatus())
                .containerId(request.getContainerId())
                .quantityValue(request.getQuantityValue())
                .quantityUnit(quantityUnit)
                .localSpecimenSystem("http://com.lims/specimen-id")
                .localSpecimenValue(localSpecimenId)
                .barcode(barcodeImage)
                .build();

        Specimen savedSpecimen = specimenRepository.save(specimen);
        return mapToSpecimenResponse(savedSpecimen);
    }

    @Transactional
    public SpecimenResponse updateSpecimen(Integer id, SpecimenUpdateRequest request) {
        Specimen specimen = specimenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specimen not found with ID: " + id));

        // --- Multi-tenancy check ---
        Integer organizationId = specimen.getPatient().getOrganization().getId();
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to update specimens for organization ID: " + organizationId);
        }
        // --- End multi-tenancy check ---

        if (request.getSpecimenTypeId() != null) {
            SpecimenType specimenType = specimenTypeRepository.findById(request.getSpecimenTypeId())
                    .orElseThrow(() -> new RuntimeException("Specimen Type not found with ID: " + request.getSpecimenTypeId()));
            specimen.setSpecimenType(specimenType);
        }
        if (request.getCollectionDate() != null) specimen.setCollectionDate(request.getCollectionDate());
        if (request.getReceivedDate() != null) specimen.setReceivedDate(request.getReceivedDate());
        if (request.getStatus() != null) specimen.setStatus(request.getStatus());
        if (request.getContainerId() != null) specimen.setContainerId(request.getContainerId());
        if (request.getQuantityValue() != null) specimen.setQuantityValue(request.getQuantityValue());
        if (request.getQuantityUnitId() != null) {
            Unit quantityUnit = unitRepository.findById(request.getQuantityUnitId())
                    .orElseThrow(() -> new RuntimeException("Quantity Unit not found with ID: " + request.getQuantityUnitId()));
            specimen.setQuantityUnit(quantityUnit);
        }

        Specimen updatedSpecimen = specimenRepository.save(specimen);
        return mapToSpecimenResponse(updatedSpecimen);
    }

    @Transactional(readOnly = true)
    public SpecimenResponse getSpecimenById(Integer id) {
        Specimen specimen = specimenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specimen not found with ID: " + id));

        // --- Multi-tenancy check ---
        Integer organizationId = specimen.getPatient().getOrganization().getId();
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to view specimens for organization ID: " + organizationId);
        }
        // --- End multi-tenancy check ---

        return mapToSpecimenResponse(specimen);
    }

    @Transactional(readOnly = true)
    public List<SpecimenResponse> getSpecimensByServiceRequest(Integer serviceRequestId) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new RuntimeException("Service Request not found with ID: " + serviceRequestId));

        // --- Multi-tenancy check ---
        Integer organizationId = serviceRequest.getPatient().getOrganization().getId();
        if (!securityService.isUserInOrganization(organizationId)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authorized to view specimens for service requests in organization ID: " + organizationId);
        }
        // --- End multi-tenancy check ---

        return specimenRepository.findByServiceRequest(serviceRequest).stream()
                .map(this::mapToSpecimenResponse)
                .collect(Collectors.toList());
    }

    private String generateLocalSpecimenId() {
        return "SP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private SpecimenResponse mapToSpecimenResponse(Specimen specimen) {
        SpecimenResponse response = new SpecimenResponse();
        response.setId(specimen.getId());
        response.setLocalSpecimenValue(specimen.getLocalSpecimenValue());
        response.setServiceRequestId(specimen.getServiceRequest().getId());
        response.setServiceRequestLocalValue(specimen.getServiceRequest().getLocalOrderValue());
        response.setPatientId(specimen.getPatient().getId());
        response.setPatientMrn(specimen.getPatient().getLocalMrnValue());
        response.setSpecimenTypeId(specimen.getSpecimenType().getId());
        response.setSpecimenTypeName(specimen.getSpecimenType().getName());
        response.setCollectionDate(specimen.getCollectionDate());
        response.setReceivedDate(specimen.getReceivedDate());
        response.setStatus(specimen.getStatus());
        response.setContainerId(specimen.getContainerId());
        response.setBarcode(specimen.getBarcode());
        response.setQuantityValue(specimen.getQuantityValue());
        if (specimen.getQuantityUnit() != null) {
            response.setQuantityUnitId(specimen.getQuantityUnit().getId());
            response.setQuantityUnitName(specimen.getQuantityUnit().getName());
        }
        response.setOrganizationId(specimen.getPatient().getOrganization().getId());
        response.setCreatedAt(specimen.getCreatedAt());
        response.setUpdatedAt(specimen.getUpdatedAt());
        return response;
    }
}

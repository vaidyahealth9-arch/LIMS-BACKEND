package com.halo.lims.controller;

import com.halo.lims.model.Patient;
import com.halo.lims.repository.PatientRepository;
import com.halo.lims.service.ExcelService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/patients/data")
public class PatientDataController {

    private final ExcelService excelService;
    private final PatientRepository patientRepository;

    public PatientDataController(ExcelService excelService, PatientRepository patientRepository) {
        this.excelService = excelService;
        this.patientRepository = patientRepository;
    }

    @PostMapping("/import")
    public ResponseEntity<String> importPatients(@RequestParam("file") MultipartFile file) {
        try {
            excelService.importPatients(file);
            return ResponseEntity.ok("Patients imported successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error importing patients: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'TECHNICIAN') and @securityService.isUserInOrganization(#organizationId)")
    public ResponseEntity<InputStreamResource> exportPatients(
            @RequestParam("organizationId") Integer organizationId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "search", required = false) String search) throws IOException {

        Specification<Patient> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("organization").get("id"), organizationId));

            if (startDate != null) {
                OffsetDateTime startDateTime = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            }

            if (endDate != null) {
                OffsetDateTime endDateTime = endDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
            }

            if (gender != null && !gender.isBlank() && !"all".equalsIgnoreCase(gender)) {
                predicates.add(cb.equal(cb.lower(root.get("gender")), gender.toLowerCase()));
            }

            if (search != null && !search.isBlank()) {
                String likePattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), likePattern),
                        cb.like(cb.lower(root.get("lastName")), likePattern),
                        cb.like(cb.lower(root.get("contactPhone")), likePattern),
                        cb.like(cb.lower(root.get("localMrnValue")), likePattern),
                        cb.like(cb.lower(root.get("abhaId")), likePattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Patient> patients = patientRepository.findAll(spec);
        ByteArrayInputStream in = excelService.exportPatients(patients);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=patients.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}

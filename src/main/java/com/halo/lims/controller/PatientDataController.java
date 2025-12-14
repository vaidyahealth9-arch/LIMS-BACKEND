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

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
    public ResponseEntity<InputStreamResource> exportPatients() throws IOException {
        List<Patient> patients = patientRepository.findAll();
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

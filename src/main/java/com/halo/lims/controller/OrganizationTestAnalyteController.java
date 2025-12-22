package com.halo.lims.controller;

import com.halo.lims.dto.organizationTestAnalyte.BulkUpdateOrganizationTestAnalyteRequest;
import com.halo.lims.dto.organizationTestAnalyte.CreateOrganizationTestAnalyteRequest;
import com.halo.lims.dto.organizationTestAnalyte.OrganizationTestAnalyteDto;
import com.halo.lims.dto.organizationTestAnalyte.UpdateOrganizationTestAnalyteRequest;
import com.halo.lims.service.OrganizationTestAnalyteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization-test-analytes")
@RequiredArgsConstructor
public class OrganizationTestAnalyteController {

    private final OrganizationTestAnalyteService organizationTestAnalyteService;

    @PostMapping
    public ResponseEntity<OrganizationTestAnalyteDto> createOrganizationTestAnalyte(@RequestBody CreateOrganizationTestAnalyteRequest request) {
        OrganizationTestAnalyteDto created = organizationTestAnalyteService.createOrganizationTestAnalyte(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{organizationId}/{testAnalyteId}")
    public ResponseEntity<OrganizationTestAnalyteDto> getOrganizationTestAnalyte(@PathVariable Integer organizationId, @PathVariable Integer testAnalyteId) {
        OrganizationTestAnalyteDto dto = organizationTestAnalyteService.getOrganizationTestAnalyte(organizationId, testAnalyteId);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<OrganizationTestAnalyteDto>> getAllOrganizationTestAnalytes() {
        List<OrganizationTestAnalyteDto> dtoList = organizationTestAnalyteService.getAllOrganizationTestAnalytes();
        return ResponseEntity.ok(dtoList);
    }

    @PutMapping("/{organizationId}/{testAnalyteId}")
    public ResponseEntity<OrganizationTestAnalyteDto> updateOrganizationTestAnalyte(@PathVariable Integer organizationId, @PathVariable Integer testAnalyteId, @RequestBody UpdateOrganizationTestAnalyteRequest request) {
        OrganizationTestAnalyteDto updated = organizationTestAnalyteService.updateOrganizationTestAnalyte(organizationId, testAnalyteId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{organizationId}/{testAnalyteId}")
    public ResponseEntity<Void> deleteOrganizationTestAnalyte(@PathVariable Integer organizationId, @PathVariable Integer testAnalyteId) {
        organizationTestAnalyteService.deleteOrganizationTestAnalyte(organizationId, testAnalyteId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/organization/{organizationId}/test/{testId}")
    public ResponseEntity<Void> bulkUpdateOrganizationTestAnalytes(@PathVariable Integer organizationId, @PathVariable Integer testId, @RequestBody BulkUpdateOrganizationTestAnalyteRequest request) {
        organizationTestAnalyteService.bulkUpdateOrganizationTestAnalytes(organizationId, testId, request.getAnalyteIds());
        return ResponseEntity.ok().build();
    }
}

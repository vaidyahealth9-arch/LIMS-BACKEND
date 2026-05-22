package com.halo.lims.controller;

import com.halo.lims.dto.test.ReferenceRangeCreateRequest;
import com.halo.lims.dto.test.ReferenceRangeResponse;
import com.halo.lims.dto.test.ReferenceRangeUpdateRequest;
import com.halo.lims.service.ReferenceRangeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reference-ranges")
public class ReferenceRangeController {

    private final ReferenceRangeService referenceRangeService;

    public ReferenceRangeController(ReferenceRangeService referenceRangeService) {
        this.referenceRangeService = referenceRangeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Only Admin can define global reference ranges
    public ResponseEntity<ReferenceRangeResponse> createReferenceRange(@Valid @RequestBody ReferenceRangeCreateRequest request) {
        ReferenceRangeResponse response = referenceRangeService.createReferenceRange(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only Admin can update global reference ranges
    public ResponseEntity<ReferenceRangeResponse> updateReferenceRange(@PathVariable Integer id, @Valid @RequestBody ReferenceRangeUpdateRequest request) {
        ReferenceRangeResponse response = referenceRangeService.updateReferenceRange(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR')") // Various roles can view
    public ResponseEntity<ReferenceRangeResponse> getReferenceRangeById(@PathVariable Integer id) {
        ReferenceRangeResponse response = referenceRangeService.getReferenceRangeById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR')")
    public ResponseEntity<List<ReferenceRangeResponse>> getAllReferenceRanges() {
        List<ReferenceRangeResponse> responses = referenceRangeService.getAllReferenceRanges();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/by-analyte/{analyteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'DOCTOR')")
    public ResponseEntity<List<ReferenceRangeResponse>> getReferenceRangesByAnalyte(@PathVariable Integer analyteId) {
        List<ReferenceRangeResponse> responses = referenceRangeService.getReferenceRangesByAnalyte(analyteId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only Admin can delete global reference ranges
    public ResponseEntity<Void> deleteReferenceRange(@PathVariable Integer id) {
        referenceRangeService.deleteReferenceRange(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

package com.halo.lims.service;

import com.halo.lims.dto.test.ReferenceRangeCreateRequest;
import com.halo.lims.dto.test.ReferenceRangeResponse;
import com.halo.lims.dto.test.ReferenceRangeUpdateRequest;
import com.halo.lims.model.ReferenceRange;
import com.halo.lims.model.TestAnalyte;
import com.halo.lims.repository.ReferenceRangeRepository;
import com.halo.lims.repository.TestAnalyteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReferenceRangeService {

    private final ReferenceRangeRepository referenceRangeRepository;
    private final TestAnalyteRepository testAnalyteRepository;

    public ReferenceRangeService(ReferenceRangeRepository referenceRangeRepository, TestAnalyteRepository testAnalyteRepository) {
        this.referenceRangeRepository = referenceRangeRepository;
        this.testAnalyteRepository = testAnalyteRepository;
    }

    @Transactional
    public ReferenceRangeResponse createReferenceRange(ReferenceRangeCreateRequest request) {
        TestAnalyte analyte = testAnalyteRepository.findById(request.getAnalyteId())
                .orElseThrow(() -> new RuntimeException("Test Analyte not found with ID: " + request.getAnalyteId()));

        ReferenceRange referenceRange = ReferenceRange.builder()
                .analyte(analyte)
                .gender(request.getGender())
                .minAgeYears(request.getMinAgeYears())
                .maxAgeYears(request.getMaxAgeYears())
                .lowValue(request.getLowValue())
                .highValue(request.getHighValue())
                .textRange(request.getTextRange())
                .interpretationCode(request.getInterpretationCode())
                .build();

        ReferenceRange savedRange = referenceRangeRepository.save(referenceRange);
        return mapToReferenceRangeResponse(savedRange);
    }

    @Transactional
    public ReferenceRangeResponse updateReferenceRange(Integer id, ReferenceRangeUpdateRequest request) {
        ReferenceRange referenceRange = referenceRangeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reference Range not found with ID: " + id));

        if (request.getGender() != null) referenceRange.setGender(request.getGender());
        if (request.getMinAgeYears() != null) referenceRange.setMinAgeYears(request.getMinAgeYears());
        if (request.getMaxAgeYears() != null) referenceRange.setMaxAgeYears(request.getMaxAgeYears());
        if (request.getLowValue() != null) referenceRange.setLowValue(request.getLowValue());
        if (request.getHighValue() != null) referenceRange.setHighValue(request.getHighValue());
        if (request.getTextRange() != null) referenceRange.setTextRange(request.getTextRange());
        if (request.getInterpretationCode() != null) referenceRange.setInterpretationCode(request.getInterpretationCode());

        ReferenceRange updatedRange = referenceRangeRepository.save(referenceRange);
        return mapToReferenceRangeResponse(updatedRange);
    }

    @Transactional(readOnly = true)
    public ReferenceRangeResponse getReferenceRangeById(Integer id) {
        ReferenceRange referenceRange = referenceRangeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reference Range not found with ID: " + id));
        return mapToReferenceRangeResponse(referenceRange);
    }

    @Transactional(readOnly = true)
    public List<ReferenceRangeResponse> getReferenceRangesByAnalyte(Integer analyteId) {
        testAnalyteRepository.findById(analyteId) // Validate analyte exists
                .orElseThrow(() -> new RuntimeException("Test Analyte not found with ID: " + analyteId));
        return referenceRangeRepository.findByAnalyteId(analyteId).stream()
                .map(this::mapToReferenceRangeResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReferenceRangeResponse> getAllReferenceRanges() {
        return referenceRangeRepository.findAll().stream()
                .map(this::mapToReferenceRangeResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReferenceRange(Integer id) {
        if (!referenceRangeRepository.existsById(id)) {
            throw new RuntimeException("Reference Range not found with ID: " + id);
        }
        referenceRangeRepository.deleteById(id);
    }

    private ReferenceRangeResponse mapToReferenceRangeResponse(ReferenceRange referenceRange) {
        ReferenceRangeResponse response = new ReferenceRangeResponse();
        response.setId(referenceRange.getId());
        response.setAnalyteId(referenceRange.getAnalyte().getId());
        response.setAnalyteCode(referenceRange.getAnalyte().getAnalyteCode());
        response.setAnalyteName(referenceRange.getAnalyte().getAnalyteName());
        response.setGender(referenceRange.getGender());
        response.setMinAgeYears(referenceRange.getMinAgeYears());
        response.setMaxAgeYears(referenceRange.getMaxAgeYears());
        response.setLowValue(referenceRange.getLowValue());
        response.setHighValue(referenceRange.getHighValue());
        response.setTextRange(referenceRange.getTextRange());
        response.setInterpretationCode(referenceRange.getInterpretationCode());
        response.setCreatedAt(referenceRange.getCreatedAt());
        response.setUpdatedAt(referenceRange.getUpdatedAt());
        return response;
    }
}

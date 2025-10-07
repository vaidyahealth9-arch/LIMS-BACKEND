package com.halo.lims.dto.serviceRequest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.halo.lims.model.ReferenceRange;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceRequestResponse {
    private Integer id;
    private String localOrderValue;
    private Integer patientId;
    private String patientMrn;
    private String patientName;
    private Integer requesterId;
    private String requesterName;
    private Integer encounterId;
    private String encounterLocalValue;
    private OffsetDateTime orderDate;
    private String status;
    private String priority;
    private Integer organizationId; // Organization of the patient
    private String organizationName;
    private List<TestDetailsResponse> requestedTests; // DTO for tests within this request
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TestDetailsResponse {
        private Integer testId;
        private String testLocalCode;
        private String testName;
        private String status; // Status of this specific item in the request
        private BigDecimal price; // Price at the time of order
        private List<AnalyteDetailsResponse> analytes;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AnalyteDetailsResponse {
        private Integer analyteId;
        private String analyteName;
        private String unit;
        private List<ReferenceRangeResponse> referenceRanges;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReferenceRangeResponse {
        private Integer id;
        private String gender;
        private Integer minAgeYears;
        private Integer maxAgeYears;
        private BigDecimal lowValue;
        private BigDecimal highValue;
        private String textRange;
        private String interpretationCode;
    }

}

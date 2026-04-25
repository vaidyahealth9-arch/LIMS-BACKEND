package com.halo.lims.dto.encounter;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.OffsetDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncounterDetailResponse {
    private Integer id;
    private Integer patientId;
    private String patientName;
    private String patientAge;
    private String patientGender;
    private String mrnId;
    private String referenceDoctor;
    private OffsetDateTime date;
    private OffsetDateTime collectionDate;
    private String sampleType;
    private String status;
    private String localEncounterValue;
    private List<String> tests;
    private List<Integer> serviceRequestIds;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientAge() { return patientAge; }
    public void setPatientAge(String patientAge) { this.patientAge = patientAge; }

    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

    public String getMrnId() { return mrnId; }
    public void setMrnId(String mrnId) { this.mrnId = mrnId; }

    public String getReferenceDoctor() { return referenceDoctor; }
    public void setReferenceDoctor(String referenceDoctor) { this.referenceDoctor = referenceDoctor; }

    public OffsetDateTime getDate() { return date; }
    public void setDate(OffsetDateTime date) { this.date = date; }

    public OffsetDateTime getCollectionDate() { return collectionDate; }
    public void setCollectionDate(OffsetDateTime collectionDate) { this.collectionDate = collectionDate; }

    public String getSampleType() { return sampleType; }
    public void setSampleType(String sampleType) { this.sampleType = sampleType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocalEncounterValue() { return localEncounterValue; }
    public void setLocalEncounterValue(String localEncounterValue) { this.localEncounterValue = localEncounterValue; }

    public List<String> getTests() { return tests; }
    public void setTests(List<String> tests) { this.tests = tests; }

    public List<Integer> getServiceRequestIds() { return serviceRequestIds; }
    public void setServiceRequestIds(List<Integer> serviceRequestIds) { this.serviceRequestIds = serviceRequestIds; }
}

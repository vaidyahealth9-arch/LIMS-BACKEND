package com.halo.lims.dto.encounter;

import java.time.OffsetDateTime;
import java.util.List;

public class EncounterListResponse {
    private Integer id;
    private String patientName;
    private String mrnId;
    private String referenceDoctor;
    private OffsetDateTime date;
    private String status;
    private List<String> tests;

    public EncounterListResponse() {}

    public EncounterListResponse(Integer id, String patientName, String mrnId, String referenceDoctor, OffsetDateTime date, String status, List<String> tests) {
        this.id = id;
        this.patientName = patientName;
        this.mrnId = mrnId;
        this.referenceDoctor = referenceDoctor;
        this.date = date;
        this.status = status;
        this.tests = tests;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getMrnId() { return mrnId; }
    public void setMrnId(String mrnId) { this.mrnId = mrnId; }

    public String getReferenceDoctor() { return referenceDoctor; }
    public void setReferenceDoctor(String referenceDoctor) { this.referenceDoctor = referenceDoctor; }

    public OffsetDateTime getDate() { return date; }
    public void setDate(OffsetDateTime date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getTests() { return tests; }
    public void setTests(List<String> tests) { this.tests = tests; }
}

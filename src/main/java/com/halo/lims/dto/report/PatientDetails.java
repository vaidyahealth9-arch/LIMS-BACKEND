package com.halo.lims.dto.report;

public class PatientDetails {
    private String patientName;
    private String age;
    private String gender;
    private String patientId;
    private String sampleId;
    private String referringDoctor;
    private String collectionDate;
    private String reportDate;
    private String serviceRequestNumber;
    private String sampleType;

    public PatientDetails() {}

    public PatientDetails(String patientName, String age, String gender, String patientId, String sampleId, String referringDoctor, String collectionDate, String reportDate, String serviceRequestNumber, String sampleType) {
        this.patientName = patientName;
        this.age = age;
        this.gender = gender;
        this.patientId = patientId;
        this.sampleId = sampleId;
        this.referringDoctor = referringDoctor;
        this.collectionDate = collectionDate;
        this.reportDate = reportDate;
        this.serviceRequestNumber = serviceRequestNumber;
        this.sampleType = sampleType;
    }

    public static PatientDetailsBuilder builder() {
        return new PatientDetailsBuilder();
    }

    // Getters and Setters
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getSampleId() { return sampleId; }
    public void setSampleId(String sampleId) { this.sampleId = sampleId; }

    public String getReferringDoctor() { return referringDoctor; }
    public void setReferringDoctor(String referringDoctor) { this.referringDoctor = referringDoctor; }

    public String getCollectionDate() { return collectionDate; }
    public void setCollectionDate(String collectionDate) { this.collectionDate = collectionDate; }

    public String getReportDate() { return reportDate; }
    public void setReportDate(String reportDate) { this.reportDate = reportDate; }

    public String getServiceRequestNumber() { return serviceRequestNumber; }
    public void setServiceRequestNumber(String serviceRequestNumber) { this.serviceRequestNumber = serviceRequestNumber; }

    public String getSampleType() { return sampleType; }
    public void setSampleType(String sampleType) { this.sampleType = sampleType; }

    public static class PatientDetailsBuilder {
        private String patientName;
        private String age;
        private String gender;
        private String patientId;
        private String sampleId;
        private String referringDoctor;
        private String collectionDate;
        private String reportDate;
        private String serviceRequestNumber;
        private String sampleType;

        public PatientDetailsBuilder patientName(String patientName) { this.patientName = patientName; return this; }
        public PatientDetailsBuilder age(String age) { this.age = age; return this; }
        public PatientDetailsBuilder gender(String gender) { this.gender = gender; return this; }
        public PatientDetailsBuilder patientId(String patientId) { this.patientId = patientId; return this; }
        public PatientDetailsBuilder sampleId(String sampleId) { this.sampleId = sampleId; return this; }
        public PatientDetailsBuilder referringDoctor(String referringDoctor) { this.referringDoctor = referringDoctor; return this; }
        public PatientDetailsBuilder collectionDate(String collectionDate) { this.collectionDate = collectionDate; return this; }
        public PatientDetailsBuilder reportDate(String reportDate) { this.reportDate = reportDate; return this; }
        public PatientDetailsBuilder serviceRequestNumber(String serviceRequestNumber) { this.serviceRequestNumber = serviceRequestNumber; return this; }
        public PatientDetailsBuilder sampleType(String sampleType) { this.sampleType = sampleType; return this; }

        public PatientDetails build() {
            return new PatientDetails(patientName, age, gender, patientId, sampleId, referringDoctor, collectionDate, reportDate, serviceRequestNumber, sampleType);
        }
    }
}
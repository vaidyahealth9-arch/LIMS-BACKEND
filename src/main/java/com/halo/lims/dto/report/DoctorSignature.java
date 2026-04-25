package com.halo.lims.dto.report;

public class DoctorSignature {
    private String doctorName;
    private String signatureImage;
    private String qualification;
    private String approvedAt;

    public DoctorSignature() {}

    public DoctorSignature(String doctorName, String signatureImage, String qualification, String approvedAt) {
        this.doctorName = doctorName;
        this.signatureImage = signatureImage;
        this.qualification = qualification;
        this.approvedAt = approvedAt;
    }

    public static DoctorSignatureBuilder builder() {
        return new DoctorSignatureBuilder();
    }

    // Getters and Setters
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getSignatureImage() { return signatureImage; }
    public void setSignatureImage(String signatureImage) { this.signatureImage = signatureImage; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getApprovedAt() { return approvedAt; }
    public void setApprovedAt(String approvedAt) { this.approvedAt = approvedAt; }

    // Minimal builder to support existing code
    public static class DoctorSignatureBuilder {
        private String doctorName;
        private String signatureImage;
        private String qualification;
        private String approvedAt;

        public DoctorSignatureBuilder doctorName(String doctorName) { this.doctorName = doctorName; return this; }
        public DoctorSignatureBuilder signatureImage(String signatureImage) { this.signatureImage = signatureImage; return this; }
        public DoctorSignatureBuilder qualification(String qualification) { this.qualification = qualification; return this; }
        public DoctorSignatureBuilder approvedAt(String approvedAt) { this.approvedAt = approvedAt; return this; }

        public DoctorSignature build() {
            return new DoctorSignature(doctorName, signatureImage, qualification, approvedAt);
        }
    }
}
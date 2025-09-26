package com.halo.lims.dto.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AbhaOtpVerificationRequest {
    @NotBlank(message = "Transaction ID is required")
    private String txnId;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
    private String otp;

    @NotBlank(message = "Authentication method is required")
    @Pattern(regexp = "MOBILE_OTP|AADHAAR_OTP|HEALTH_ID", message = "Invalid authentication method")
    private String authMethod;
}

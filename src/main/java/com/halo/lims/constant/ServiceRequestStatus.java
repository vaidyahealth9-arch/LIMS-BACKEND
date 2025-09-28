package com.halo.lims.constant;

import java.util.Arrays;
import java.util.Optional;

/**
 * FHIR ServiceRequestStatus (from http://hl7.org/fhir/R4/codesystem-request-status.html)
 */
public enum ServiceRequestStatus {
    DRAFT("draft", "The request is in draft form only, can be subject to change, is not yet authorized."),
    ACTIVE("active", "The request is ready to be acted upon."),
    ON_HOLD("on-hold", "The request has been held (actively or passively) for some reason and processing should stop."),
    REVOKED("revoked", "The request has been determined to be unsuitable for the intended purpose and should not be acted upon."),
    COMPLETED("completed", "The request has been completed."),
    ENTERED_IN_ERROR("entered-in-error", "This instance should not have been part of this patient's medical record."),
    UNKNOWN("unknown", "The current status of the request is not known.");

    private final String code;
    private final String display;

    ServiceRequestStatus(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public String getCode() {
        return code;
    }

    public String getDisplay() {
        return display;
    }

    public static Optional<ServiceRequestStatus> fromCode(String code) {
        return Arrays.stream(ServiceRequestStatus.values())
                .filter(status -> status.code.equalsIgnoreCase(code))
                .findFirst();
    }
}

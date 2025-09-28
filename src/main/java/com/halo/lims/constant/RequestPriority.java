package com.halo.lims.constant;

import java.util.Arrays;
import java.util.Optional;

/**
 * FHIR RequestPriority (from http://hl7.org/fhir/R4/codesystem-request-priority.html)
 */
public enum RequestPriority {
    ROUTINE("routine", "The request has normal priority."),
    URGENT("urgent", "The request should be acted on, but not immediately. An urgent request will eventually be acted on."),
    ASAP("asap", "The request should be acted on as soon as possible."),
    STAT("stat", "The request should be acted on immediately and without waiting.");

    private final String code;
    private final String display;

    RequestPriority(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public String getCode() {
        return code;
    }

    public String getDisplay() {
        return display;
    }

    public static Optional<RequestPriority> fromCode(String code) {
        return Arrays.stream(RequestPriority.values())
                .filter(priority -> priority.code.equalsIgnoreCase(code))
                .findFirst();
    }
}

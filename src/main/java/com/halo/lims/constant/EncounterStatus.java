package com.halo.lims.constant;

import java.util.Arrays;
import java.util.Optional;

/**
 * FHIR EncounterStatus (from http://hl7.org/fhir/R4/codesystem-encounter-status.html)
 */
public enum EncounterStatus {
    PLANNED("planned", "The Encounter has not yet begun."),
    ARRIVED("arrived", "The Patient is present for the encounter."),
    TRIAGED("triaged", "The patient has been triaged, but the encounter has not yet started."),
    IN_PROGRESS("in-progress", "The Encounter has begun and the patient is receiving care."),
    ON_LEAVE("onleave", "The patient is temporarily on leave from the facility."),
    CANCELLED("cancelled", "The Encounter has been cancelled and is no longer planned to occur."),
    ENTERED_IN_ERROR("entered-in-error", "This instance should not have been part of this patient's medical record."),
    UNKNOWN("unknown", "The current status of the encounter is not known."),
    // Custom status for LIMS workflow
    PENDING_BILLING("pending-billing", "The encounter is finished, but billing is pending."),
    PENDING_VERIFICATION("pending-verification", "The encounter is awaiting doctor verification."),
    APPROVED("approved", "The encounter results have been approved by the doctor."),
    COMPLETED("completed", "The encounter workflow is fully complete and closed.");

    private final String code;
    private final String display;

    EncounterStatus(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public String getCode() {
        return code;
    }

    public String getDisplay() {
        return display;
    }

    public static Optional<EncounterStatus> fromCode(String code) {
        return Arrays.stream(EncounterStatus.values())
                .filter(status -> status.code.equalsIgnoreCase(code))
                .findFirst();
    }
}

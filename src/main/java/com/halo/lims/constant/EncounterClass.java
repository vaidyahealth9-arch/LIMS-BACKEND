package com.halo.lims.constant;

import java.util.Arrays;
import java.util.Optional;

/**
 * FHIR EncounterClass (from http://terminology.hl7.org/CodeSystem/v3-ActCode)
 * Subset relevant for LIMS.
 */
public enum EncounterClass {
    AMBULATORY("AMB", "Ambulatory (outpatient) encounter"),
    INPATIENT("IMP", "Inpatient encounter"),
    EMERGENCY("EMER", "Emergency encounter"),
    FIELD("FLD", "Field encounter"),
    HOME_HEALTH("HH", "Home Health"),
    OUTPATIENT("OUTPT", "Outpatient encounter"),
    WELLNESS("WELL", "Wellness encounter"),
    OTHER("OTHER", "Other type of encounter");

    private final String code;
    private final String display;

    EncounterClass(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public String getCode() {
        return code;
    }

    public String getDisplay() {
        return display;
    }

    public static Optional<EncounterClass> fromCode(String code) {
        return Arrays.stream(EncounterClass.values())
                .filter(eClass -> eClass.code.equalsIgnoreCase(code))
                .findFirst();
    }
}

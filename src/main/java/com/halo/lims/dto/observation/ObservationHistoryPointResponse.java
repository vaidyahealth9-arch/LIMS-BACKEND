package com.halo.lims.dto.observation;

public class ObservationHistoryPointResponse {
    private Double value;
    private String effectiveDateTime;
    private Integer observationId;

    public ObservationHistoryPointResponse() {}

    public ObservationHistoryPointResponse(Double value, String effectiveDateTime, Integer observationId) {
        this.value = value;
        this.effectiveDateTime = effectiveDateTime;
        this.observationId = observationId;
    }

    // Getters and Setters
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public String getEffectiveDateTime() { return effectiveDateTime; }
    public void setEffectiveDateTime(String effectiveDateTime) { this.effectiveDateTime = effectiveDateTime; }

    public Integer getObservationId() { return observationId; }
    public void setObservationId(Integer observationId) { this.observationId = observationId; }
}

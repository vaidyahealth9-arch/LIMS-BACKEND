package com.halo.lims.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard API error response for all LIMS endpoints.
 * 
 * Ensures consistent error handling across the service.
 * All exceptions are mapped to this schema for client consumption.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized API error response")
public class ApiErrorResponse {
    
    @Schema(description = "ISO 8601 timestamp when error occurred", example = "2025-01-15T10:30:45.123")
    private LocalDateTime timestamp;
    
    @Schema(description = "HTTP status code", example = "400")
    private int status;
    
    @Schema(description = "Machine-readable error code for client handling", example = "INVALID_REQUEST")
    private String error;
    
    @Schema(description = "Human-readable error message", example = "Patient ID is required")
    private String message;
    
    @Schema(description = "Request path that generated the error", example = "/api/v1/patients")
    private String path;
    
    @Schema(description = "Correlation ID for distributed tracing", example = "550e8400-e29b-41d4-a716-446655440000")
    private String traceId;
    
    @Schema(description = "Additional error details", example = "{\"field\": \"patient_id\", \"constraint\": \"NotNull\"}")
    private Map<String, Object> details;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, String traceId, Map<String, Object> details) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.traceId = traceId;
        this.details = details;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
    
    public static ApiErrorResponse of(int status, String error, String message, String path, String traceId) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, traceId, null);
    }
    
    public static ApiErrorResponse of(int status, String error, String message, String path, String traceId, Map<String, Object> details) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, traceId, details);
    }
}

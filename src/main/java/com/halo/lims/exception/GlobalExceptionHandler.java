package com.halo.lims.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Access Denied: You do not have permission to access this resource",
                request,
                null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiErrorResponse response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request,
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = null;
        String normalizedMessage = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (normalizedMessage.contains("username already exists")) {
                fieldErrors = new LinkedHashMap<>();
                fieldErrors.put("username", ex.getMessage());
        }

        ApiErrorResponse response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request,
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (msg.contains("not found")) {
            status = HttpStatus.NOT_FOUND;
        }

        ApiErrorResponse response = buildErrorResponse(status, message, request, null);
        return ResponseEntity.status(status).body(response);
    }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
                        HttpRequestMethodNotSupportedException ex,
                        HttpServletRequest request
        ) {
                String message = ex.getMessage() != null ? ex.getMessage() : "Request method is not supported";
                ApiErrorResponse response = buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, message, request, null);
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
        }

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleNoResourceFound(
                        NoResourceFoundException ex,
                        HttpServletRequest request
        ) {
                String message = ex.getMessage() != null ? ex.getMessage() : "Requested resource not found";
                ApiErrorResponse response = buildErrorResponse(HttpStatus.NOT_FOUND, message, request, null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
                        DataIntegrityViolationException ex,
                        HttpServletRequest request
        ) {
                String rawMessage = ex.getMostSpecificCause() != null
                        ? ex.getMostSpecificCause().getMessage()
                        : ex.getMessage();
                String normalizedMessage = rawMessage != null ? rawMessage.toLowerCase() : "";

                String message;
                Map<String, String> fieldErrors = null;
                if (normalizedMessage.contains("username") || normalizedMessage.contains("lims_user_username")) {
                        message = "Username already exists. Please choose a different username.";
                        fieldErrors = new LinkedHashMap<>();
                        fieldErrors.put("username", message);
                } else if (normalizedMessage.contains("local_identifier_value") || normalizedMessage.contains("practitioners_local_identifier_value")) {
                        message = "Practitioner identifier generation conflicted with an existing record. Please retry creating the user.";
                } else if (normalizedMessage.contains("practitioner_id") || normalizedMessage.contains("lims_user_practitioner_id")) {
                        message = "This practitioner is already linked to another user account.";
                } else if (normalizedMessage.contains("signature_image_asset_id")) {
                        message = "Operation failed while saving the user signature image. Please try again.";
                } else if (normalizedMessage.contains("uk_patients_contact_phone_normalized")
                        || normalizedMessage.contains("contact_phone_normalized")) {
                        message = "A patient with this mobile number already exists.";
                } else if (normalizedMessage.contains("duplicate key") || normalizedMessage.contains("already exists")) {
                        message = "Operation failed because a record with the same unique value already exists. Please review duplicate IDs/codes and try again.";
                } else if (normalizedMessage.contains("foreign key") || normalizedMessage.contains("is still referenced")) {
                        message = "Operation failed due to related records. Please remove dependencies first.";
                } else if (rawMessage != null && !rawMessage.isBlank()) {
                        message = "Operation failed due to data integrity constraints: " + rawMessage;
                } else {
                        message = "Operation failed due to data integrity constraints.";
                }
                ApiErrorResponse response = buildErrorResponse(HttpStatus.CONFLICT, message, request, fieldErrors);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
                request,
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ApiErrorResponse buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors
    ) {
        return new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request != null ? request.getRequestURI() : null,
                fieldErrors
        );
    }
}

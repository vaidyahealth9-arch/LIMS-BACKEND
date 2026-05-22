package com.halo.lims.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom exception for business logic errors
 */
public class ServiceException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;
    
    public ServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    
    public ServiceException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
    
    public ServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    
    public ServiceException(String message, String errorCode, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public HttpStatus getStatus() {
        return status;
    }
}

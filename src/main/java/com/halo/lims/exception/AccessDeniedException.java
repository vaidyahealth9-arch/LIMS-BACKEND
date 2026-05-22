package com.halo.lims.exception;

/**
 * Exception thrown when a user does not have permission to access a resource
 */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
    
    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}

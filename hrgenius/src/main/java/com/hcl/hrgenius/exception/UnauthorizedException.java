package com.hcl.hrgenius.exception;

/**
 * Exception thrown when authentication or credential validation fails.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}

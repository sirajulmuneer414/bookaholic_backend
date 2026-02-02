package com.bookaholic.backend.exceptions;

public class UnableToRegisterException extends RuntimeException {
    public UnableToRegisterException(String message) {
        super(message);
    }
}

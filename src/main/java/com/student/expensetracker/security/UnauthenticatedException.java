package com.student.expensetracker.security;

/** Maps to HTTP 401 via the global exception handler. */
public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException(String message) {
        super(message);
    }
}
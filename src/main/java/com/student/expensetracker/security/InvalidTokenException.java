package com.student.expensetracker.security;

/** Signals a token that is missing, malformed, expired or wrongly signed. */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
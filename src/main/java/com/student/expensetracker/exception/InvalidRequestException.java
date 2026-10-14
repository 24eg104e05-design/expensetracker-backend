package com.student.expensetracker.exception;

/** Maps to HTTP 400 for semantically invalid but well-formed requests. */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
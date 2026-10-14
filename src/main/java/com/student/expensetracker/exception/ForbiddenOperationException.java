package com.student.expensetracker.exception;

/** Maps to HTTP 403 (e.g. attempting to modify a global default category). */
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
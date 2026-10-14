package com.student.expensetracker.exception;

/** Maps to HTTP 409 (duplicate email, duplicate category name). */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
package com.student.expensetracker.exception;

/** Maps to HTTP 409 (e.g. deleting a category that still has expenses attached). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
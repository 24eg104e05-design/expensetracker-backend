package com.student.expensetracker.dto;

/** Response body for POST /api/auth/login (PRD 7.1). */
public record AuthResponse(String token, long expiresInSeconds, UserResponse user) {
}
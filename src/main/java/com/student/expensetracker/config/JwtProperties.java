package com.student.expensetracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code app.jwt.*} (PRD 4.1 / NFR Security). */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expiration, String issuer) {
}
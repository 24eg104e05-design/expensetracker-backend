package com.student.expensetracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code app.cors.*} -- explicit origins only, never wildcard (PRD 5). */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(String allowedOrigins) {

    public String[] originArray() {
        return allowedOrigins == null || allowedOrigins.isBlank()
                ? new String[0]
                : allowedOrigins.split("\\s*,\\s*");
    }
}
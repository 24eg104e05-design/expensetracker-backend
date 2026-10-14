package com.student.expensetracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code app.currency.*}. Amounts are stored as bare numbers; only display is localized. */
@ConfigurationProperties(prefix = "app.currency")
public record CurrencyProperties(String symbol, String code, String locale) {
}
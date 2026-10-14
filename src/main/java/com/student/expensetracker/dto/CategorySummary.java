package com.student.expensetracker.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** One slice of GET /api/analytics/category-summary (PRD 7.3). */
public record CategorySummary(
        Long categoryId,
        String categoryName,
        String colorHex,
        BigDecimal totalAmount,
        BigDecimal percentage,
        long expenseCount) {

    public static CategorySummary of(
            Long categoryId, String name, String colorHex, BigDecimal total, long count, BigDecimal grandTotal) {

        BigDecimal percentage = grandTotal.signum() == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : total.multiply(BigDecimal.valueOf(100))
                        .divide(grandTotal, 2, RoundingMode.HALF_UP);

        return new CategorySummary(categoryId, name, colorHex, total, percentage, count);
    }
}
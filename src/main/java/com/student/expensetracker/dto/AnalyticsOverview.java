package com.student.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** KPI cards for the dashboard (PRD 4.4): Total Spent, Top Category, Average Daily Spend. */
public record AnalyticsOverview(
        BigDecimal totalSpent,
        String topCategoryName,
        String topCategoryColorHex,
        BigDecimal topCategoryAmount,
        BigDecimal averageDailySpend,
        long distinctDaysWithSpending,
        int inclusiveDayCount,
        LocalDate startDate,
        LocalDate endDate,
        String currencyCode,
        String currencySymbol,
        List<CategorySummary> categories) {
}
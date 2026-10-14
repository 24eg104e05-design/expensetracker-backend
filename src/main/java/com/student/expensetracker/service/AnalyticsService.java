package com.student.expensetracker.service;

import com.student.expensetracker.config.CurrencyProperties;
import com.student.expensetracker.dto.AnalyticsOverview;
import com.student.expensetracker.dto.CategorySummary;
import com.student.expensetracker.repository.ExpenseRepository;
import com.student.expensetracker.repository.projection.CategorySpend;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aggregation behind the pie chart and the KPI cards (PRD 4.4 / 7.3). */
@Service
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final CurrencyProperties currency;

    public AnalyticsService(ExpenseRepository expenseRepository, CurrencyProperties currency) {
        this.expenseRepository = expenseRepository;
        this.currency = currency;
    }

    @Transactional(readOnly = true)
    public List<CategorySummary> categorySummary(Long userId, LocalDate startDate, LocalDate endDate) {
        return summaryFor(userId, startDate, endDate, null);
    }

    /**
     * @param categoryId optional. The dashboard passes it for the KPI cards so
     *                   the totals match the filtered table, and omits it for the
     *                   pie chart, which must stay unfiltered to remain usable
     *                   as the control that sets the category filter.
     */
    @Transactional(readOnly = true)
    public AnalyticsOverview overview(Long userId, LocalDate startDate, LocalDate endDate, Long categoryId) {
        LocalDate effectiveStart = startDate;
        LocalDate effectiveEnd = endDate;

        // With no explicit window, fall back to the span of the student's own
        // data so "average daily" stays meaningful instead of dividing by one day.
        if (effectiveStart == null || effectiveEnd == null) {
            LocalDate first = expenseRepository.findFirstExpenseDate(userId).orElse(null);
            LocalDate last = expenseRepository.findLastExpenseDate(userId).orElse(null);
            if (first != null && last != null) {
                if (effectiveStart == null) {
                    effectiveStart = first;
                }
                if (effectiveEnd == null) {
                    effectiveEnd = last;
                }
            }
        }
        if (effectiveStart == null || effectiveEnd == null) {
            // No expenses at all: fall back to today so the response stays well-formed.
            LocalDate today = LocalDate.now();
            if (effectiveStart == null) {
                effectiveStart = today;
            }
            if (effectiveEnd == null) {
                effectiveEnd = today;
            }
        }

        List<CategorySummary> summaries = summaryFor(userId, effectiveStart, effectiveEnd, categoryId);
        BigDecimal total = expenseRepository.sumAmountInRange(userId, effectiveStart, effectiveEnd, categoryId);
        total = total == null ? BigDecimal.ZERO : total;

        CategorySummary top = summaries.isEmpty() ? null : summaries.get(0);
        long distinctDays = expenseRepository.countDistinctDaysInRange(
                userId, effectiveStart, effectiveEnd, categoryId);

        long inclusiveDays = Math.max(
                1, ChronoUnit.DAYS.between(effectiveStart, effectiveEnd) + 1);
        BigDecimal averageDaily = total.divide(BigDecimal.valueOf(inclusiveDays), 2, RoundingMode.HALF_UP);

        return new AnalyticsOverview(
                total.setScale(2, RoundingMode.HALF_UP),
                top == null ? null : top.categoryName(),
                top == null ? null : top.colorHex(),
                top == null ? null : top.totalAmount(),
                averageDaily,
                distinctDays,
                (int) inclusiveDays,
                effectiveStart,
                effectiveEnd,
                currency.code(),
                currency.symbol(),
                summaries);
    }

    private List<CategorySummary> summaryFor(Long userId, LocalDate startDate, LocalDate endDate, Long categoryId) {
        List<CategorySpend> rows = expenseRepository.sumByCategory(userId, startDate, endDate, categoryId);
        BigDecimal grandTotal = rows.stream()
                .map(CategorySpend::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return rows.stream()
                .map(row -> CategorySummary.of(
                        row.getCategoryId(),
                        row.getCategoryName(),
                        row.getColorHex(),
                        row.getTotalAmount().setScale(2, RoundingMode.HALF_UP),
                        row.getExpenseCount() == null ? 0 : row.getExpenseCount(),
                        grandTotal))
                .toList();
    }
}
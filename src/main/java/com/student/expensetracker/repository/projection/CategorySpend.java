package com.student.expensetracker.repository.projection;

import java.math.BigDecimal;

/**
 * Row of the per-category aggregation behind the dashboard pie chart (PRD 7.3).
 * Populated by aliasing the JPQL select list -- see ExpenseRepository.
 */
public interface CategorySpend {

    Long getCategoryId();

    String getCategoryName();

    String getColorHex();

    BigDecimal getTotalAmount();

    Long getExpenseCount();
}
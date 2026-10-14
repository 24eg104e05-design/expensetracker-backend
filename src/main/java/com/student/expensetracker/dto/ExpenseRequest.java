package com.student.expensetracker.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must be at most 150 characters")
        String title,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @DecimalMax(value = "99999999.99", message = "Amount exceeds the maximum supported value")
        BigDecimal amount,

        @NotNull(message = "Category is required")
        Long categoryId,

        @NotNull(message = "Date is required")
        @PastOrPresent(message = "Expense date cannot be in the future")
        LocalDate date,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description) {
}
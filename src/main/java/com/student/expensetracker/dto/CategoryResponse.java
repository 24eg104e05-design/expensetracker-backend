package com.student.expensetracker.dto;

import com.student.expensetracker.entity.Category;

public record CategoryResponse(
        Long id,
        String name,
        String colorHex,
        boolean global) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColorHex(),
                category.isGlobal());
    }
}
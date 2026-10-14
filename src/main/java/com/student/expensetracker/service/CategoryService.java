package com.student.expensetracker.service;

import com.student.expensetracker.dto.CategoryRequest;
import com.student.expensetracker.dto.CategoryResponse;
import com.student.expensetracker.entity.Category;
import com.student.expensetracker.entity.User;
import com.student.expensetracker.exception.ConflictException;
import com.student.expensetracker.exception.DuplicateResourceException;
import com.student.expensetracker.exception.ForbiddenOperationException;
import com.student.expensetracker.exception.ResourceNotFoundException;
import com.student.expensetracker.repository.CategoryRepository;
import com.student.expensetracker.repository.ExpenseRepository;
import com.student.expensetracker.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Categories: the seven preloaded global defaults (PRD 4.3) plus any custom
 * category the signed-in student creates for themselves.
 */
@Service
public class CategoryService {

    private static final String DEFAULT_COLOR = "#0071E3";

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            ExpenseRepository expenseRepository,
            UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(Long userId) {
        return categoryRepository.findVisibleTo(userId).stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Category requireVisible(Long categoryId, Long userId) {
        return categoryRepository.findVisibleById(categoryId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", categoryId));
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.findByUserAndName(userId, name).isPresent()) {
            throw new DuplicateResourceException("You already have a category named \"" + name + "\"");
        }
        User user = userRepository.getReferenceById(userId);
        Category category = Category.builder()
                .name(name)
                .colorHex(normalizeColor(request.colorHex()))
                .user(user)
                .build();
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long userId, Long categoryId, CategoryRequest request) {
        Category category = requireOwned(userId, categoryId);
        String name = request.name().trim();
        categoryRepository.findByUserAndName(userId, name)
                .filter(other -> !other.getId().equals(categoryId))
                .ifPresent(other -> {
                    throw new DuplicateResourceException("You already have a category named \"" + name + "\"");
                });
        category.setName(name);
        category.setColorHex(normalizeColor(request.colorHex()));
        return CategoryResponse.from(categoryRepository.save(category));
    }

    /** Global defaults are immutable; only a student's own categories can be removed. */
    @Transactional
    public void delete(Long userId, Long categoryId) {
        Category category = requireOwned(userId, categoryId);
        long inUse = expenseRepository.countOwnedByCategory(userId, categoryId);
        if (inUse > 0) {
            throw new ConflictException(
                    "This category is used by " + inUse + " expense" + (inUse == 1 ? "" : "s")
                            + ". Reassign or delete " + (inUse == 1 ? "it" : "them") + " first.");
        }
        categoryRepository.delete(category);
    }

    private Category requireOwned(Long userId, Long categoryId) {
        Category category = categoryRepository.findVisibleById(categoryId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", categoryId));
        if (category.isGlobal()) {
            throw new ForbiddenOperationException("Default categories cannot be modified or deleted");
        }
        return category;
    }

    private static String normalizeColor(String colorHex) {
        return colorHex == null || colorHex.isBlank() ? DEFAULT_COLOR : colorHex.toUpperCase();
    }
}
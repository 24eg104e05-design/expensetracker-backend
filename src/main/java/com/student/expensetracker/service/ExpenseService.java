package com.student.expensetracker.service;

import com.student.expensetracker.dto.ExpenseRequest;
import com.student.expensetracker.dto.ExpenseResponse;
import com.student.expensetracker.dto.PageResponse;
import com.student.expensetracker.entity.Category;
import com.student.expensetracker.entity.Expense;
import com.student.expensetracker.entity.User;
import com.student.expensetracker.exception.InvalidRequestException;
import com.student.expensetracker.exception.ResourceNotFoundException;
import com.student.expensetracker.repository.ExpenseRepository;
import com.student.expensetracker.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Expense CRUD. Every method takes the caller's user id from the security
 * context -- ownership is never inferred from the request payload (PRD 4.1/4.2).
 */
@Service
public class ExpenseService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            CategoryService categoryService) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> list(
            Long userId, int page, int size, LocalDate startDate, LocalDate endDate, Long categoryId) {

        validateRange(startDate, endDate);
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "expenseDate").and(Sort.by(Sort.Direction.DESC, "id")));

        Page<Expense> result = expenseRepository.findFiltered(userId, startDate, endDate, categoryId, pageable);
        return PageResponse.from(result.map(ExpenseResponse::from));
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long userId, Long id) {
        return ExpenseResponse.from(requireOwned(userId, id));
    }

    @Transactional
    public ExpenseResponse create(Long userId, ExpenseRequest request) {
        Category category = categoryService.requireVisible(request.categoryId(), userId);
        User user = userRepository.getReferenceById(userId);

        Expense expense = Expense.builder()
                .user(user)
                .category(category)
                .title(request.title().trim())
                .amount(normalizeAmount(request.amount()))
                .expenseDate(request.date())
                .description(blankToNull(request.description()))
                .build();
        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse update(Long userId, Long id, ExpenseRequest request) {
        Expense expense = requireOwned(userId, id);
        Category category = categoryService.requireVisible(request.categoryId(), userId);

        expense.setTitle(request.title().trim());
        expense.setAmount(normalizeAmount(request.amount()));
        expense.setCategory(category);
        expense.setExpenseDate(request.date());
        expense.setDescription(blankToNull(request.description()));
        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        expenseRepository.delete(requireOwned(userId, id));
    }

    /** Backed by {@code idx_user_date}; feeds the CSV export (PRD 4.5). */
    @Transactional(readOnly = true)
    public java.util.List<Expense> listForExport(Long userId, LocalDate startDate, LocalDate endDate, Long categoryId) {
        validateRange(startDate, endDate);
        return expenseRepository.findFilteredForExport(userId, startDate, endDate, categoryId);
    }

    private Expense requireOwned(Long userId, Long id) {
        return expenseRepository.findOwnedById(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Expense", id));
    }

    private static void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidRequestException("startDate must not be after endDate");
        }
    }

    private static BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
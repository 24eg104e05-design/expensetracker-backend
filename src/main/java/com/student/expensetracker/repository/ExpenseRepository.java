package com.student.expensetracker.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.student.expensetracker.entity.Expense;
import com.student.expensetracker.repository.projection.CategorySpend;

/**
 * Every query here is scoped by {@code userId}. This is the primary defence for
 * the multi-tenancy rule in PRD 4.1 -- ownership is never taken from the request
 * body, only from the authenticated principal.
 */
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    String FILTERED = """
            WHERE e.user.id = :userId
              AND e.expenseDate >= COALESCE(:startDate, e.expenseDate)
              AND e.expenseDate <= COALESCE(:endDate, e.expenseDate)
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
            """;

    @Query(value = "SELECT e FROM Expense e " + FILTERED, countQuery = "SELECT COUNT(e) FROM Expense e " + FILTERED)
    Page<Expense> findFiltered(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    /** Category is fetched eagerly: the CSV writer streams after the session closes. */
    @Query("""
            SELECT e FROM Expense e JOIN FETCH e.category
            WHERE e.user.id = :userId
              AND e.expenseDate >= COALESCE(:startDate, e.expenseDate)
              AND e.expenseDate <= COALESCE(:endDate, e.expenseDate)
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
            ORDER BY e.expenseDate ASC, e.id ASC
            """)
    List<Expense> findFilteredForExport(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId);

    @Query("SELECT e FROM Expense e WHERE e.id = :id AND e.user.id = :userId")
    Optional<Expense> findOwnedById(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId AND e.category.id = :categoryId")
    long countOwnedByCategory(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    /** Per-category totals backing GET /api/analytics/category-summary (PRD 7.3). */
    @Query("""
            SELECT c.id AS categoryId,
                   c.name AS categoryName,
                   c.colorHex AS colorHex,
                   SUM(e.amount) AS totalAmount,
                   COUNT(e) AS expenseCount
            FROM Expense e JOIN e.category c
            WHERE e.user.id = :userId
              AND e.expenseDate >= COALESCE(:startDate, e.expenseDate)
              AND e.expenseDate <= COALESCE(:endDate, e.expenseDate)
              AND (:categoryId IS NULL OR c.id = :categoryId)
            GROUP BY c.id, c.name, c.colorHex
            ORDER BY SUM(e.amount) DESC
            """)
    List<CategorySpend> sumByCategory(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId);

    /** Total spend across the filtered window (KPI: Total Spent). */
    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.user.id = :userId
              AND e.expenseDate >= COALESCE(:startDate, e.expenseDate)
              AND e.expenseDate <= COALESCE(:endDate, e.expenseDate)
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
            """)
    BigDecimal sumAmountInRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId);

    @Query("""
            SELECT COUNT(DISTINCT e.expenseDate)
            FROM Expense e
            WHERE e.user.id = :userId
              AND e.expenseDate >= COALESCE(:startDate, e.expenseDate)
              AND e.expenseDate <= COALESCE(:endDate, e.expenseDate)
              AND (:categoryId IS NULL OR e.category.id = :categoryId)
            """)
    long countDistinctDaysInRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId);

    /** Fallback window when the caller supplies no dates: the user's own span. */
    @Query("SELECT MIN(e.expenseDate) FROM Expense e WHERE e.user.id = :userId")
    Optional<LocalDate> findFirstExpenseDate(@Param("userId") Long userId);

    @Query("SELECT MAX(e.expenseDate) FROM Expense e WHERE e.user.id = :userId")
    Optional<LocalDate> findLastExpenseDate(@Param("userId") Long userId);
}
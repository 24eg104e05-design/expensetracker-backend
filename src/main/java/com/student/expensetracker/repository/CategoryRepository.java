package com.student.expensetracker.repository;

import com.student.expensetracker.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Global defaults (seeded first by id) plus the authenticated user's own (PRD 4.3). */
    @Query("""
            SELECT c FROM Category c
            WHERE c.user IS NULL OR c.user.id = :userId
            ORDER BY c.id
            """)
    List<Category> findVisibleTo(@Param("userId") Long userId);

    @Query("SELECT COUNT(c) FROM Category c WHERE c.user IS NULL AND LOWER(c.name) = LOWER(:name)")
    long countGlobalByName(@Param("name") String name);

    /** Resolves a category the given user is allowed to reference. */
    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.user IS NULL OR c.user.id = :userId)")
    Optional<Category> findVisibleById(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND LOWER(c.name) = LOWER(:name)")
    Optional<Category> findByUserAndName(@Param("userId") Long userId, @Param("name") String name);
}
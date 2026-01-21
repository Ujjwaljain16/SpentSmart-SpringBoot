package com.expenseTracker.demo.repository;

import com.expenseTracker.demo.entity.Category;
import com.expenseTracker.demo.entity.Expense;
import com.expenseTracker.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    Optional<Expense> findByIdAndUserAndIsDeletedFalse(UUID id, User user);

    Page<Expense> findByUserAndIsDeletedFalse(User user, Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.user = :user AND e.isDeleted = false " +
           "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
           "AND (:startDate IS NULL OR e.expenseDate >= :startDate) " +
           "AND (:endDate IS NULL OR e.expenseDate <= :endDate) " +
           "AND (:minAmount IS NULL OR e.amount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR e.amount <= :maxAmount)")
    Page<Expense> findByFilters(
            @Param("user") User user,
            @Param("categoryId") UUID categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable
    );

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.isDeleted = false " +
           "AND EXTRACT(MONTH FROM e.expenseDate) = :month AND EXTRACT(YEAR FROM e.expenseDate) = :year")
    BigDecimal calculateMonthlyTotal(
            @Param("user") User user,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("SELECT e.category.name as category, SUM(e.amount) as total FROM Expense e " +
           "WHERE e.user = :user AND e.isDeleted = false " +
           "AND EXTRACT(MONTH FROM e.expenseDate) = :month AND EXTRACT(YEAR FROM e.expenseDate) = :year " +
           "GROUP BY e.category.name ORDER BY total DESC")
    List<Map<String, Object>> getCategoryBreakdown(
            @Param("user") User user,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("SELECT e.expenseDate as date, SUM(e.amount) as total FROM Expense e " +
           "WHERE e.user = :user AND e.isDeleted = false " +
           "AND EXTRACT(MONTH FROM e.expenseDate) = :month AND EXTRACT(YEAR FROM e.expenseDate) = :year " +
           "GROUP BY e.expenseDate ORDER BY e.expenseDate ASC")
    List<Map<String, Object>> getDailyTrend(
            @Param("user") User user,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("SELECT e FROM Expense e WHERE e.user = :user AND e.isDeleted = false " +
           "ORDER BY e.amount DESC LIMIT 1")
    Optional<Expense> findHighestExpense(@Param("user") User user);

    long countByUserAndIsDeletedFalse(User user);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user = :user AND e.isDeleted = false " +
           "AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate")
    long countByDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    boolean existsByCategoryAndIsDeletedFalse(Category category);
}

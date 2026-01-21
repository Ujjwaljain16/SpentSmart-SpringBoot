package com.expenseTracker.demo.repository;

import com.expenseTracker.demo.entity.Category;
import com.expenseTracker.demo.entity.CategoryBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget, UUID> {

    Optional<CategoryBudget> findByCategory(Category category);

    boolean existsByCategory(Category category);
}

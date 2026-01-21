package com.expenseTracker.demo.service;

import com.expenseTracker.demo.entity.Category;
import com.expenseTracker.demo.entity.CategoryBudget;
import com.expenseTracker.demo.entity.User;
import com.expenseTracker.demo.repository.CategoryBudgetRepository;
import com.expenseTracker.demo.repository.CategoryRepository;
import com.expenseTracker.demo.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetAlertService {

    private final CategoryBudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final EmailService emailService;

    @Transactional
    @Async
    public void checkBudgetThreshold(UUID categoryId) {
        try {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category == null) return;

            CategoryBudget budget = budgetRepository.findByCategory(category).orElse(null);
            if (budget == null) return;

            LocalDate now = LocalDate.now();
            BigDecimal monthlySpending = expenseRepository.calculateMonthlyTotal(
                category.getUser(), now.getMonthValue(), now.getYear()
            );

            if (monthlySpending == null) return;

            BigDecimal threshold = budget.getMonthlyLimit()
                    .multiply(BigDecimal.valueOf(budget.getAlertThreshold()))
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

            if (monthlySpending.compareTo(threshold) >= 0 && !budget.getAlertSent()) {
                sendBudgetAlert(category.getUser(), category, monthlySpending, budget.getMonthlyLimit());
                budget.setAlertSent(true);
                budgetRepository.save(budget);
            }
        } catch (Exception e) {
            log.error("Error checking budget threshold for category: {}", categoryId, e);
        }
    }

    private void sendBudgetAlert(User user, Category category, BigDecimal current, BigDecimal limit) {
        try {
            String subject = "Budget Alert: " + category.getName();
            String body = String.format(
                "Hello %s,\n\n" +
                "Your spending in the '%s' category has reached $%s, which is %.0f%% of your monthly budget of $%s.\n\n" +
                "Consider reviewing your expenses to stay within budget.\n\n" +
                "Best regards,\nExpense Tracker Team",
                user.getFullName() != null ? user.getFullName() : user.getEmail(),
                category.getName(),
                current,
                current.divide(limit, 2, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue(),
                limit
            );

            emailService.sendEmail(user.getEmail(), subject, body);
            log.info("Budget alert sent to user: {} for category: {}", user.getEmail(), category.getName());
        } catch (Exception e) {
            log.error("Failed to send budget alert", e);
        }
    }

    @Transactional
    public void resetMonthlyAlerts() {
        budgetRepository.findAll().forEach(budget -> {
            budget.setAlertSent(false);
            budgetRepository.save(budget);
        });
        log.info("Reset all monthly budget alerts");
    }
}

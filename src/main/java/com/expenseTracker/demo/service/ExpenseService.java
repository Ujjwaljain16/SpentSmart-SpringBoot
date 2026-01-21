package com.expenseTracker.demo.service;

import com.expenseTracker.demo.dto.request.ExpenseRequest;
import com.expenseTracker.demo.dto.response.ExpenseResponse;
import com.expenseTracker.demo.entity.Category;
import com.expenseTracker.demo.entity.Expense;
import com.expenseTracker.demo.entity.User;
import com.expenseTracker.demo.exception.ResourceNotFoundException;
import com.expenseTracker.demo.repository.CategoryRepository;
import com.expenseTracker.demo.repository.ExpenseRepository;
import com.expenseTracker.demo.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetAlertService budgetAlertService;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional
    @CacheEvict(value = {Constants.Cache.ANALYTICS_MONTHLY, Constants.Cache.ANALYTICS_CATEGORY}, allEntries = true)
    public ExpenseResponse createExpense(ExpenseRequest request) {
        User user = getCurrentUser();
        
        Category category = categoryRepository.findByIdAndUser(request.getCategoryId(), user)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ErrorMessages.CATEGORY_NOT_FOUND));

        Expense expense = Expense.builder()
                .user(user)
                .category(category)
                .amount(request.getAmount())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .isDeleted(false)
                .build();

        expense = expenseRepository.save(expense);

        budgetAlertService.checkBudgetThreshold(category.getId());

        return ExpenseResponse.from(expense);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(UUID id) {
        User user = getCurrentUser();
        
        Expense expense = expenseRepository.findByIdAndUserAndIsDeletedFalse(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ErrorMessages.EXPENSE_NOT_FOUND));

        return ExpenseResponse.from(expense);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getAllExpenses(
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        
        User user = getCurrentUser();
        
        size = Math.min(size, Constants.Pagination.MAX_PAGE_SIZE);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return expenseRepository.findByUserAndIsDeletedFalse(user, pageable)
                .map(ExpenseResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getExpensesWithFilters(
            UUID categoryId,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        
        User user = getCurrentUser();
        
        size = Math.min(size, Constants.Pagination.MAX_PAGE_SIZE);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return expenseRepository.findByFilters(
                user, categoryId, startDate, endDate, minAmount, maxAmount, pageable
        ).map(ExpenseResponse::from);
    }

    @Transactional
    @CacheEvict(value = {Constants.Cache.ANALYTICS_MONTHLY, Constants.Cache.ANALYTICS_CATEGORY}, allEntries = true)
    public ExpenseResponse updateExpense(UUID id, ExpenseRequest request) {
        User user = getCurrentUser();
        
        Expense expense = expenseRepository.findByIdAndUserAndIsDeletedFalse(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ErrorMessages.EXPENSE_NOT_FOUND));

        Category category = categoryRepository.findByIdAndUser(request.getCategoryId(), user)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ErrorMessages.CATEGORY_NOT_FOUND));

        expense.setCategory(category);
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setNotes(request.getNotes());

        expense = expenseRepository.save(expense);
        return ExpenseResponse.from(expense);
    }

    @Transactional
    @CacheEvict(value = {Constants.Cache.ANALYTICS_MONTHLY, Constants.Cache.ANALYTICS_CATEGORY}, allEntries = true)
    public void deleteExpense(UUID id) {
        User user = getCurrentUser();
        
        Expense expense = expenseRepository.findByIdAndUserAndIsDeletedFalse(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ErrorMessages.EXPENSE_NOT_FOUND));

        expense.setIsDeleted(true);
        expenseRepository.save(expense);
    }
}

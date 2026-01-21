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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BudgetAlertService budgetAlertService;

    @InjectMocks
    private ExpenseService expenseService;

    private User user;
    private Category category;
    private Expense expense;
    private ExpenseRequest expenseRequest;

    @BeforeEach
    void setUp() {
        // Mock Security Context
        user = User.builder()
                .email("test@example.com")
                .fullName("Test User")
                .role(User.Role.USER)
                .build();
        user.setId(UUID.randomUUID());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );

        category = Category.builder()
                .name("Food")
                .user(user)
                .build();
        category.setId(UUID.randomUUID());

        expense = Expense.builder()
                .amount(new BigDecimal("100.00"))
                .description("Lunch")
                .category(category)
                .user(user)
                .expenseDate(LocalDate.now())
                .isDeleted(false)
                .build();
        expense.setId(UUID.randomUUID());

        expenseRequest = new ExpenseRequest();
        expenseRequest.setAmount(new BigDecimal("100.00"));
        expenseRequest.setDescription("Lunch");
        expenseRequest.setCategoryId(category.getId());
        expenseRequest.setExpenseDate(LocalDate.now());
        expenseRequest.setPaymentMethod(Expense.PaymentMethod.CASH);
    }

    @Test
    @DisplayName("Should create expense successfully")
    void createExpense_Success() {
        when(categoryRepository.findByIdAndUser(any(UUID.class), any(User.class)))
                .thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        ExpenseResponse response = expenseService.createExpense(expenseRequest);

        assertNotNull(response);
        assertEquals(expense.getAmount(), response.getAmount());
        assertEquals(expense.getDescription(), response.getDescription());
        
        verify(budgetAlertService).checkBudgetThreshold(category.getId());
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void createExpense_CategoryNotFound() {
        when(categoryRepository.findByIdAndUser(any(UUID.class), any(User.class)))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            expenseService.createExpense(expenseRequest)
        );
        
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    @DisplayName("Should get expense by ID successfully")
    void getExpenseById_Success() {
        when(expenseRepository.findByIdAndUserAndIsDeletedFalse(any(UUID.class), any(User.class)))
                .thenReturn(Optional.of(expense));

        ExpenseResponse response = expenseService.getExpenseById(expense.getId());

        assertNotNull(response);
        assertEquals(expense.getId(), response.getId());
    }

    @Test
    @DisplayName("Should soft delete expense successfully")
    void deleteExpense_Success() {
        when(expenseRepository.findByIdAndUserAndIsDeletedFalse(any(UUID.class), any(User.class)))
                .thenReturn(Optional.of(expense));

        expenseService.deleteExpense(expense.getId());

        verify(expenseRepository).save(expense);
        assertTrue(expense.getIsDeleted());
    }
}

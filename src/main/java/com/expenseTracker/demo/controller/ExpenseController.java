package com.expenseTracker.demo.controller;

import com.expenseTracker.demo.dto.request.ExpenseRequest;
import com.expenseTracker.demo.dto.response.ExpenseResponse;
import com.expenseTracker.demo.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Expense management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('USER')")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Create expense", description = "Create a new expense for the authenticated user")
    @ApiResponse(responseCode = "201", description = "Expense created successfully")
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID", description = "Retrieve a specific expense by ID")
    @ApiResponse(responseCode = "200", description = "Expense retrieved successfully")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable UUID id) {
        ExpenseResponse response = expenseService.getExpenseById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List expenses", description = "Get paginated list of expenses with optional filters")
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    public ResponseEntity<Page<ExpenseResponse>> getExpenses(
            @Parameter(description = "Category ID filter") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Start date filter (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date filter (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Minimum amount filter") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Maximum amount filter") @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "expenseDate") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection) {

        Page<ExpenseResponse> response;
        
        if (categoryId != null || startDate != null || endDate != null || minAmount != null || maxAmount != null) {
            response = expenseService.getExpensesWithFilters(
                    categoryId, startDate, endDate, minAmount, maxAmount, page, size, sortBy, sortDirection);
        } else {
            response = expenseService.getAllExpenses(page, size, sortBy, sortDirection);
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update expense", description = "Update an existing expense")
    @ApiResponse(responseCode = "200", description = "Expense updated successfully")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable UUID id,
            @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.updateExpense(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete expense", description = "Soft delete an expense")
    @ApiResponse(responseCode = "204", description = "Expense deleted successfully")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}

package com.expenseTracker.demo.dto.request;

import com.expenseTracker.demo.entity.Expense;
import com.expenseTracker.demo.util.Constants;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequest {

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal amount;

    @Size(max = Constants.Validation.DESCRIPTION_MAX_LENGTH, message = "Description is too long")
    private String description;

    @NotNull(message = "Expense date is required")
    @PastOrPresent(message = "Expense date cannot be in the future")
    private LocalDate expenseDate;

    private Expense.PaymentMethod paymentMethod;

    @Size(max = Constants.Validation.DESCRIPTION_MAX_LENGTH, message = "Notes are too long")
    private String notes;
}

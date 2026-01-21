package com.expenseTracker.demo.entity;

import com.expenseTracker.demo.util.Constants;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses",
    indexes = {
        @Index(name = "idx_expense_user_date", columnList = "user_id, expense_date DESC"),
        @Index(name = "idx_expense_user_category", columnList = "user_id, category_id"),
        @Index(name = "idx_expense_user_amount", columnList = "user_id, amount")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = Constants.Validation.DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(length = Constants.Validation.DESCRIPTION_MAX_LENGTH)
    private String notes;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public enum PaymentMethod {
        CASH, CARD, UPI, NET_BANKING, OTHER
    }
}

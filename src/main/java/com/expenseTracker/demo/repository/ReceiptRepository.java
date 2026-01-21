package com.expenseTracker.demo.repository;

import com.expenseTracker.demo.entity.Expense;
import com.expenseTracker.demo.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {

    Optional<Receipt> findByExpense(Expense expense);

    boolean existsByExpense(Expense expense);
}

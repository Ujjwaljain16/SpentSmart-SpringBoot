package com.expenseTracker.demo.repository;

import com.expenseTracker.demo.entity.Category;
import com.expenseTracker.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByUserOrderByNameAsc(User user);

    Optional<Category> findByIdAndUser(UUID id, User user);

    boolean existsByUserAndName(User user, String name);

    long countByUser(User user);
}

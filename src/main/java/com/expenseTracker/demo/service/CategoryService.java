package com.expenseTracker.demo.service;

import com.expenseTracker.demo.dto.request.CategoryRequest;
import com.expenseTracker.demo.dto.response.CategoryResponse;
import com.expenseTracker.demo.entity.Category;
import com.expenseTracker.demo.entity.User;
import com.expenseTracker.demo.exception.ResourceNotFoundException;
import com.expenseTracker.demo.repository.CategoryRepository;
import com.expenseTracker.demo.repository.ExpenseRepository;
import com.expenseTracker.demo.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional
    @CacheEvict(value = Constants.Cache.CATEGORIES_USER, allEntries = true)
    public CategoryResponse createCategory(CategoryRequest request) {
        User user = getCurrentUser();

        if (categoryRepository.existsByUserAndName(user, request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .colorCode(request.getColorCode())
                .build();

        category = categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = Constants.Cache.CATEGORIES_USER, key = "#root.target.getCurrentUser().id")
    public List<CategoryResponse> getAllCategories() {
        User user = getCurrentUser();
        
        return categoryRepository.findByUserOrderByNameAsc(user).stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        User user = getCurrentUser();
        
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ErrorMessages.CATEGORY_NOT_FOUND));

        return CategoryResponse.from(category);
    }

    @Transactional
    @CacheEvict(value = {Constants.Cache.CATEGORIES_USER, Constants.Cache.ANALYTICS_CATEGORY}, allEntries = true)
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        User user = getCurrentUser();
        
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ErrorMessages.CATEGORY_NOT_FOUND));

        if (!category.getName().equals(request.getName()) &&
            categoryRepository.existsByUserAndName(user, request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setColorCode(request.getColorCode());

        category = categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    @Transactional
    @CacheEvict(value = {Constants.Cache.CATEGORIES_USER, Constants.Cache.ANALYTICS_CATEGORY}, allEntries = true)
    public void deleteCategory(UUID id) {
        User user = getCurrentUser();
        
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ErrorMessages.CATEGORY_NOT_FOUND));

        if (expenseRepository.existsByCategoryAndIsDeletedFalse(category)) {
            throw new IllegalArgumentException("Cannot delete category with existing expenses");
        }

        categoryRepository.delete(category);
    }
}

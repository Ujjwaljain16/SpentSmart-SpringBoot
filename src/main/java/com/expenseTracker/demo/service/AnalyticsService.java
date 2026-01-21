package com.expenseTracker.demo.service;

import com.expenseTracker.demo.dto.response.CategoryBreakdownItem;
import com.expenseTracker.demo.dto.response.ExpenseResponse;
import com.expenseTracker.demo.dto.response.InsightsResponse;
import com.expenseTracker.demo.dto.response.MonthlySummaryResponse;
import com.expenseTracker.demo.entity.Expense;
import com.expenseTracker.demo.entity.User;
import com.expenseTracker.demo.repository.ExpenseRepository;
import com.expenseTracker.demo.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = Constants.Cache.ANALYTICS_MONTHLY, key = "#month + '-' + #year + '-' + #root.target.getCurrentUser().id")
    public MonthlySummaryResponse getMonthlySummary(int month, int year) {
        User user = getCurrentUser();
        
        BigDecimal total = expenseRepository.calculateMonthlyTotal(user, month, year);
        long count = expenseRepository.countByDateRange(
                user, 
                LocalDate.of(year, month, 1),
                LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)
        );

        return MonthlySummaryResponse.builder()
                .month(month)
                .year(year)
                .totalExpenses(total != null ? total : BigDecimal.ZERO)
                .expenseCount(count)
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = Constants.Cache.ANALYTICS_CATEGORY, key = "#month + '-' + #year + '-' + #root.target.getCurrentUser().id")
    public List<CategoryBreakdownItem> getCategoryBreakdown(int month, int year) {
        User user = getCurrentUser();
        
        List<Map<String, Object>> breakdown = expenseRepository.getCategoryBreakdown(user, month, year);
        
        return breakdown.stream()
                .map(item -> CategoryBreakdownItem.builder()
                        .categoryName((String) item.get("category"))
                        .totalAmount((BigDecimal) item.get("total"))
                        .expenseCount(0L)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDailyTrend(int month, int year) {
        User user = getCurrentUser();
        return expenseRepository.getDailyTrend(user, month, year);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getHighestExpense() {
        User user = getCurrentUser();
        
        return expenseRepository.findHighestExpense(user)
                .map(ExpenseResponse::from)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public InsightsResponse getInsights() {
        User user = getCurrentUser();
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        
        int previousMonth = currentMonth == 1 ? 12 : currentMonth - 1;
        int previousYear = currentMonth == 1 ? currentYear - 1 : currentYear;

        BigDecimal currentTotal = expenseRepository.calculateMonthlyTotal(user, currentMonth, currentYear);
        BigDecimal previousTotal = expenseRepository.calculateMonthlyTotal(user, previousMonth, previousYear);

        currentTotal = currentTotal != null ? currentTotal : BigDecimal.ZERO;
        previousTotal = previousTotal != null ? previousTotal : BigDecimal.ZERO;

        InsightsResponse.MonthlyComparison comparison = buildMonthlyComparison(currentTotal, previousTotal);
        List<InsightsResponse.CategoryInsight> categoryInsights = buildCategoryInsights(user, currentMonth, currentYear);
        InsightsResponse.HighestExpenseInfo highestExpense = buildHighestExpenseInfo(user);
        String summary = buildSummary(comparison, categoryInsights);

        return InsightsResponse.builder()
                .monthlyComparison(comparison)
                .categoryInsights(categoryInsights)
                .highestExpense(highestExpense)
                .summary(summary)
                .build();
    }

    private InsightsResponse.MonthlyComparison buildMonthlyComparison(BigDecimal current, BigDecimal previous) {
        BigDecimal difference = current.subtract(previous);
        Double percentageChange = previous.compareTo(BigDecimal.ZERO) > 0 
            ? difference.divide(previous, 2, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
            : 0.0;

        String trend = percentageChange > 0 ? "INCREASED" : percentageChange < 0 ? "DECREASED" : "STABLE";

        return InsightsResponse.MonthlyComparison.builder()
                .currentMonthTotal(current)
                .previousMonthTotal(previous)
                .difference(difference)
                .percentageChange(percentageChange)
                .trend(trend)
                .build();
    }

    private List<InsightsResponse.CategoryInsight> buildCategoryInsights(User user, int month, int year) {
        List<Map<String, Object>> breakdown = expenseRepository.getCategoryBreakdown(user, month, year);
        
        BigDecimal totalSpending = breakdown.stream()
                .map(item -> (BigDecimal) item.get("total"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averagePerCategory = breakdown.isEmpty() ? BigDecimal.ZERO 
                : totalSpending.divide(BigDecimal.valueOf(breakdown.size()), 2, java.math.RoundingMode.HALF_UP);

        return breakdown.stream()
                .map(item -> {
                    String categoryName = (String) item.get("category");
                    BigDecimal amount = (BigDecimal) item.get("total");
                    
                    BigDecimal diff = amount.subtract(averagePerCategory);
                    Double percentageAbove = averagePerCategory.compareTo(BigDecimal.ZERO) > 0
                        ? diff.divide(averagePerCategory, 2, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                        : 0.0;

                    String status = percentageAbove > 25 ? "HIGH" : percentageAbove < -25 ? "LOW" : "NORMAL";

                    return InsightsResponse.CategoryInsight.builder()
                            .categoryName(categoryName)
                            .currentAmount(amount)
                            .averageAmount(averagePerCategory)
                            .percentageAboveAverage(percentageAbove)
                            .status(status)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private InsightsResponse.HighestExpenseInfo buildHighestExpenseInfo(User user) {
        return expenseRepository.findHighestExpense(user)
                .map(expense -> InsightsResponse.HighestExpenseInfo.builder()
                        .amount(expense.getAmount())
                        .description(expense.getDescription())
                        .date(expense.getExpenseDate().toString())
                        .categoryName(expense.getCategory() != null ? expense.getCategory().getName() : "Uncategorized")
                        .build())
                .orElse(null);
    }

    private String buildSummary(InsightsResponse.MonthlyComparison comparison, List<InsightsResponse.CategoryInsight> insights) {
        StringBuilder summary = new StringBuilder();
        
        if (comparison.getPercentageChange() > 10) {
            summary.append(String.format("Your spending increased by %.1f%% compared to last month. ", 
                Math.abs(comparison.getPercentageChange())));
        } else if (comparison.getPercentageChange() < -10) {
            summary.append(String.format("Great job! Your spending decreased by %.1f%% compared to last month. ", 
                Math.abs(comparison.getPercentageChange())));
        }

        long highCategories = insights.stream().filter(i -> "HIGH".equals(i.getStatus())).count();
        if (highCategories > 0) {
            summary.append(String.format("%d category(ies) exceeded average spending by more than 25%%. ", highCategories));
        }

        return summary.toString().trim();
    }
}

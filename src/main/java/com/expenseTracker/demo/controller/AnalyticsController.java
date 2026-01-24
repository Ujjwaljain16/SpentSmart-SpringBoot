package com.expenseTracker.demo.controller;

import com.expenseTracker.demo.dto.response.CategoryBreakdownItem;
import com.expenseTracker.demo.dto.response.ExpenseResponse;
import com.expenseTracker.demo.dto.response.InsightsResponse;
import com.expenseTracker.demo.dto.response.MonthlySummaryResponse;
import com.expenseTracker.demo.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Expense analytics and reporting endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('USER')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final com.expenseTracker.demo.service.EmailService emailService;

    @GetMapping("/monthly-summary")
    @Operation(summary = "Monthly summary", description = "Get total expenses and count for a specific month")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @Parameter(description = "Month (1-12)") @RequestParam int month,
            @Parameter(description = "Year") @RequestParam int year) {
        MonthlySummaryResponse response = analyticsService.getMonthlySummary(month, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category-breakdown")
    @Operation(summary = "Category breakdown", description = "Get spending breakdown by category for a specific month")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Breakdown retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryBreakdownItem>> getCategoryBreakdown(
            @Parameter(description = "Month (1-12)") @RequestParam int month,
            @Parameter(description = "Year") @RequestParam int year) {
        List<CategoryBreakdownItem> response = analyticsService.getCategoryBreakdown(month, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/daily-trend")
    @Operation(summary = "Daily spending trend", description = "Get daily spending trend for a specific month")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trend retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<Map<String, Object>>> getDailyTrend(
            @Parameter(description = "Month (1-12)") @RequestParam int month,
            @Parameter(description = "Year") @RequestParam int year) {
        List<Map<String, Object>> response = analyticsService.getDailyTrend(month, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/highest-expense")
    @Operation(summary = "Highest expense", description = "Get the highest expense for the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Highest expense retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ExpenseResponse> getHighestExpense() {
        ExpenseResponse response = analyticsService.getHighestExpense();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/insights")
    @Operation(summary = "Smart insights", description = "Get intelligent spending insights with trend analysis and category comparisons")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Insights retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InsightsResponse> getInsights() {
        InsightsResponse response = analyticsService.getInsights();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-email")
    @Operation(summary = "Test Email", description = "Trigger a monthly report email to the authenticated user for verification")
    public ResponseEntity<String> testEmail(@org.springframework.security.core.annotation.AuthenticationPrincipal com.expenseTracker.demo.entity.User user) {
        emailService.sendMonthlyReport(user);
        return ResponseEntity.ok("Email trigger initiated. Check server logs for 'Monthly report sent'.");
    }
}

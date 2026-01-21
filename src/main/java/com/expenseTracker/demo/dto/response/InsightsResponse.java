package com.expenseTracker.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Smart insights response with trend analysis")
public class InsightsResponse {

    @Schema(description = "Monthly spending comparison")
    private MonthlyComparison monthlyComparison;
    
    @Schema(description = "Category-wise spending insights")
    private List<CategoryInsight> categoryInsights;
    
    @Schema(description = "Highest expense information")
    private HighestExpenseInfo highestExpense;
    
    @Schema(description = "Personalized summary message")
    private String summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Monthly comparison data")
    public static class MonthlyComparison {
        private BigDecimal currentMonthTotal;
        private BigDecimal previousMonthTotal;
        private BigDecimal difference;
        private Double percentageChange;
        private String trend;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Category spending insight")
    public static class CategoryInsight {
        private String categoryName;
        private BigDecimal currentAmount;
        private BigDecimal averageAmount;
        private Double percentageAboveAverage;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Highest expense details")
    public static class HighestExpenseInfo {
        private BigDecimal amount;
        private String description;
        private String date;
        private String categoryName;
    }
}

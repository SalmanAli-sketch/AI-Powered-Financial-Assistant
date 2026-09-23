package com.fintrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryResponse {
    private BigDecimal totalSpent;
    private BigDecimal budgetLimit;
    private BigDecimal budgetRemaining;
    private double utilizationPercentage;
    private String topCategoryName;
    private BigDecimal topCategoryAmount;
}

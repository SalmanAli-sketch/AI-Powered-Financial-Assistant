package com.fintrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetUsageResponse {
    private YearMonth month;
    private BigDecimal limit;
    private BigDecimal totalSpent;
    private BigDecimal remaining;
    private double percentageSpent;
}

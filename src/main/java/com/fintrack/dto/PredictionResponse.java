package com.fintrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {
    private BigDecimal currentMonthSpent;
    private BigDecimal projectedSpent;
    private BigDecimal budgetLimit;
    private boolean projectedToExceed;
    private int daysRemainingInMonth;
}

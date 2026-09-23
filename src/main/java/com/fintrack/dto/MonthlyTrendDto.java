package com.fintrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendDto {
    private YearMonth month;
    private BigDecimal totalSpent;
}

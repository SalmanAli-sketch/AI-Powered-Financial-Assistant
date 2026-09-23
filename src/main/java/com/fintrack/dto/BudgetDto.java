package com.fintrack.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDto {
    private Long id;

    @NotNull(message = "Monthly limit is required")
    @DecimalMin(value = "0.01", message = "Monthly limit must be greater than zero")
    private BigDecimal monthlyLimit;

    @NotNull(message = "Month is required")
    private YearMonth month; // e.g., "2026-05"
}

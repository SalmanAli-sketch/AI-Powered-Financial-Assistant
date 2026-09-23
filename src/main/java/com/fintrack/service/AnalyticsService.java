package com.fintrack.service;

import com.fintrack.dto.AnalyticsSummaryResponse;
import com.fintrack.dto.CategoryBreakdownDto;
import com.fintrack.dto.MonthlyTrendDto;
import com.fintrack.entity.Budget;
import com.fintrack.entity.Category;
import com.fintrack.entity.Expense;
import com.fintrack.entity.User;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.BudgetRepository;
import com.fintrack.repository.ExpenseRepository;
import com.fintrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    public AnalyticsService(ExpenseRepository expenseRepository,
                            BudgetRepository budgetRepository,
                            UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
    }

    public AnalyticsSummaryResponse getMonthlySummary(YearMonth month, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BigDecimal budgetLimit = budgetRepository.findByUserAndMonth(user, month)
                .map(Budget::getMonthlyLimit)
                .orElse(BigDecimal.ZERO);

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, start, end);

        BigDecimal totalSpent = BigDecimal.valueOf(
                expenses.stream().mapToDouble(Expense::getAmount).sum()
        );

        BigDecimal budgetRemaining = budgetLimit.subtract(totalSpent);
        double utilization = 0.0;
        if (budgetLimit.compareTo(BigDecimal.ZERO) > 0) {
            utilization = totalSpent.divide(budgetLimit, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        // Find top category
        Map<String, Double> categoryMap = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.summingDouble(Expense::getAmount)
                ));

        String topCategoryName = "N/A";
        BigDecimal topCategoryAmount = BigDecimal.ZERO;
        if (!categoryMap.isEmpty()) {
            Map.Entry<String, Double> top = Collections.max(
                    categoryMap.entrySet(),
                    Map.Entry.comparingByValue()
            );
            topCategoryName = top.getKey();
            topCategoryAmount = BigDecimal.valueOf(top.getValue());
        }

        return new AnalyticsSummaryResponse(
                totalSpent,
                budgetLimit,
                budgetRemaining,
                utilization,
                topCategoryName,
                topCategoryAmount
        );
    }

    public List<CategoryBreakdownDto> getCategoryBreakdown(YearMonth month, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, start, end);

        double totalSpentVal = expenses.stream().mapToDouble(Expense::getAmount).sum();
        if (totalSpentVal == 0.0) {
            return Collections.emptyList();
        }

        Map<String, Double> categorySums = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.summingDouble(Expense::getAmount)
                ));

        return categorySums.entrySet().stream()
                .map(entry -> {
                    BigDecimal spent = BigDecimal.valueOf(entry.getValue());
                    double pct = (entry.getValue() / totalSpentVal) * 100.0;
                    // Round percentage to 2 decimal places
                    pct = Math.round(pct * 100.0) / 100.0;
                    return new CategoryBreakdownDto(entry.getKey(), spent, pct);
                })
                .sorted(Comparator.comparing(CategoryBreakdownDto::getTotalSpent).reversed())
                .collect(Collectors.toList());
    }

    public List<MonthlyTrendDto> getSpendingTrends(int limitMonths, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        YearMonth currentMonth = YearMonth.now();
        List<MonthlyTrendDto> trends = new ArrayList<>();

        for (int i = 0; i < limitMonths; i++) {
            YearMonth targetMonth = currentMonth.minusMonths(i);
            LocalDate start = targetMonth.atDay(1);
            LocalDate end = targetMonth.atEndOfMonth();
            List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, start, end);
            BigDecimal totalSpent = BigDecimal.valueOf(
                    expenses.stream().mapToDouble(Expense::getAmount).sum()
            );
            trends.add(new MonthlyTrendDto(targetMonth, totalSpent));
        }

        // Reverse trends to make them chronological (oldest to newest)
        Collections.reverse(trends);
        return trends;
    }
}

package com.fintrack.service;

import com.fintrack.dto.PredictionResponse;
import com.fintrack.entity.Budget;
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
import java.util.List;

@Service
public class PredictionService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    public PredictionService(ExpenseRepository expenseRepository,
                             BudgetRepository budgetRepository,
                             UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
    }

    public PredictionResponse getSpendingProjection(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        YearMonth currentMonth = YearMonth.now();
        LocalDate today = LocalDate.now();
        int dayOfMonth = today.getDayOfMonth();
        int totalDays = currentMonth.lengthOfMonth();

        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();
        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, start, end);

        BigDecimal currentSpent = BigDecimal.valueOf(
                expenses.stream().mapToDouble(Expense::getAmount).sum()
        );

        BigDecimal budgetLimit = budgetRepository.findByUserAndMonth(user, currentMonth)
                .map(Budget::getMonthlyLimit)
                .orElse(BigDecimal.ZERO);

        BigDecimal projectedSpent;
        if (dayOfMonth > 0) {
            BigDecimal avgDaily = currentSpent.divide(BigDecimal.valueOf(dayOfMonth), 4, RoundingMode.HALF_UP);
            projectedSpent = avgDaily.multiply(BigDecimal.valueOf(totalDays)).setScale(2, RoundingMode.HALF_UP);
        } else {
            projectedSpent = currentSpent;
        }

        boolean projectedToExceed = false;
        if (budgetLimit.compareTo(BigDecimal.ZERO) > 0) {
            projectedToExceed = projectedSpent.compareTo(budgetLimit) > 0;
        }

        int daysRemaining = totalDays - dayOfMonth;

        return new PredictionResponse(
                currentSpent,
                projectedSpent,
                budgetLimit,
                projectedToExceed,
                daysRemaining
        );
    }
}

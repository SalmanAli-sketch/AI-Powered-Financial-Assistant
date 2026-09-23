package com.fintrack.service;

import com.fintrack.dto.BudgetDto;
import com.fintrack.dto.BudgetUsageResponse;
import com.fintrack.entity.Budget;
import com.fintrack.entity.Expense;
import com.fintrack.entity.User;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.mapper.BudgetMapper;
import com.fintrack.repository.BudgetRepository;
import com.fintrack.repository.ExpenseRepository;
import com.fintrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final BudgetMapper budgetMapper;

    public BudgetService(BudgetRepository budgetRepository,
                         ExpenseRepository expenseRepository,
                         UserRepository userRepository,
                         BudgetMapper budgetMapper) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.budgetMapper = budgetMapper;
    }

    public BudgetDto setBudget(BudgetDto dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Budget> existing = budgetRepository.findByUserAndMonth(user, dto.getMonth());
        Budget budget;
        if (existing.isPresent()) {
            budget = existing.get();
            budget.setMonthlyLimit(dto.getMonthlyLimit());
        } else {
            budget = budgetMapper.toEntity(dto);
            budget.setUser(user);
        }

        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toDto(saved);
    }

    public BudgetDto getBudget(YearMonth month, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Budget budget = budgetRepository.findByUserAndMonth(user, month)
                .orElseThrow(() -> new ResourceNotFoundException("No budget set for " + month));

        return budgetMapper.toDto(budget);
    }

    public BudgetUsageResponse getBudgetUsage(YearMonth month, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BigDecimal limit = budgetRepository.findByUserAndMonth(user, month)
                .map(Budget::getMonthlyLimit)
                .orElse(BigDecimal.ZERO);

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, start, end);

        BigDecimal totalSpent = BigDecimal.valueOf(
                expenses.stream().mapToDouble(Expense::getAmount).sum()
        );

        BigDecimal remaining = limit.subtract(totalSpent);
        double percentage = 0.0;
        if (limit.compareTo(BigDecimal.ZERO) > 0) {
            percentage = totalSpent.divide(limit, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        return new BudgetUsageResponse(month, limit, totalSpent, remaining, percentage);
    }
}

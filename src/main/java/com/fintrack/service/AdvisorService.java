package com.fintrack.service;

import com.fintrack.dto.SuggestionResponse;
import com.fintrack.entity.Budget;
import com.fintrack.entity.Expense;
import com.fintrack.entity.User;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.BudgetRepository;
import com.fintrack.repository.ExpenseRepository;
import com.fintrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdvisorService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    public AdvisorService(ExpenseRepository expenseRepository,
                          BudgetRepository budgetRepository,
                          UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
    }

    public SuggestionResponse getSuggestions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        List<String> advices = new ArrayList<>();

        // Get current month stats
        LocalDate currentStart = currentMonth.atDay(1);
        LocalDate currentEnd = currentMonth.atEndOfMonth();
        List<Expense> currentExpenses = expenseRepository.findByUserAndDateBetween(user, currentStart, currentEnd);
        double currentSpent = currentExpenses.stream().mapToDouble(Expense::getAmount).sum();

        Optional<Budget> budgetOpt = budgetRepository.findByUserAndMonth(user, currentMonth);

        // Budget check
        if (budgetOpt.isPresent()) {
            double budgetLimit = budgetOpt.get().getMonthlyLimit().doubleValue();
            if (budgetLimit > 0.0) {
                if (currentSpent > budgetLimit) {
                    advices.add(String.format("Warning: You have exceeded your budget limit of $%.2f by $%.2f!", budgetLimit, (currentSpent - budgetLimit)));
                } else if (currentSpent >= budgetLimit * 0.8) {
                    advices.add(String.format("Notice: You have used %.1f%% of your monthly budget limit. Try to defer non-essential purchases.", (currentSpent / budgetLimit * 100)));
                } else {
                    advices.add("Good job! You are well under your budget limit for this month.");
                }
            }
        } else {
            advices.add("Recommendation: You haven't set a budget limit for this month. Setting a monthly limit helps you stay on track!");
        }

        // Compare to last month
        LocalDate prevStart = previousMonth.atDay(1);
        LocalDate prevEnd = previousMonth.atEndOfMonth();
        List<Expense> prevExpenses = expenseRepository.findByUserAndDateBetween(user, prevStart, prevEnd);
        double prevSpent = prevExpenses.stream().mapToDouble(Expense::getAmount).sum();

        if (prevSpent > 0.0) {
            double diffPct = ((currentSpent - prevSpent) / prevSpent) * 100;
            if (diffPct > 10.0) {
                advices.add(String.format("Alert: Your spending is %.1f%% higher than last month. Review your categories to see where expenses spiked.", diffPct));
            } else if (diffPct < -10.0) {
                advices.add(String.format("Great news! You are spending %.1f%% less than last month. Excellent progress!", Math.abs(diffPct)));
            }
        }

        // Category breakdown alerts
        if (!currentExpenses.isEmpty()) {
            Map<String, Double> categoryMap = currentExpenses.stream()
                    .collect(Collectors.groupingBy(
                            e -> e.getCategory().getName(),
                            Collectors.summingDouble(Expense::getAmount)
                    ));

            Map.Entry<String, Double> topCategory = Collections.max(
                    categoryMap.entrySet(),
                    Map.Entry.comparingByValue()
            );

            if (topCategory.getValue() > currentSpent * 0.3) {
                advices.add(String.format("Tip: %s is your top spending category, accounting for %.1f%% of your total monthly expenses. Setting a cap on this category is recommended.", 
                        topCategory.getKey(), (topCategory.getValue() / currentSpent * 100)));
            }
        }

        if (advices.isEmpty()) {
            advices.add("No specific savings recommendations at this time. Maintain your current spending habits.");
        }

        return new SuggestionResponse(advices);
    }
}

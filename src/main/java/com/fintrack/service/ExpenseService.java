package com.fintrack.service;

import com.fintrack.dto.ExpenseDto;
import com.fintrack.entity.Category;
import com.fintrack.entity.Expense;
import com.fintrack.entity.User;
import com.fintrack.exception.BadRequestException;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.mapper.ExpenseMapper;
import com.fintrack.repository.CategoryRepository;
import com.fintrack.repository.ExpenseRepository;
import com.fintrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;
    private final BudgetService budgetService;
    private final NotificationService notificationService;

    public ExpenseService(ExpenseRepository expenseRepository,
                          UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          ExpenseMapper expenseMapper,
                          BudgetService budgetService,
                          NotificationService notificationService) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.expenseMapper = expenseMapper;
        this.budgetService = budgetService;
        this.notificationService = notificationService;
    }

    public ExpenseDto createExpense(ExpenseDto dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findByName(dto.getCategoryName())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryName()));

        Expense expense = expenseMapper.toEntity(dto);
        expense.setUser(user);
        expense.setCategory(category);

        Expense saved = expenseRepository.save(expense);
        
        // Trigger real-time budget check
        checkBudgetAndNotify(email, saved.getDate());

        return expenseMapper.toDto(saved);
    }

    private void checkBudgetAndNotify(String email, LocalDate date) {
        try {
            java.time.YearMonth ym = java.time.YearMonth.from(date);
            com.fintrack.dto.BudgetUsageResponse usage = budgetService.getBudgetUsage(ym, email);
            if (usage.getLimit().compareTo(java.math.BigDecimal.ZERO) > 0) {
                double pct = usage.getPercentageSpent();
                if (pct >= 100.0) {
                    notificationService.sendNotification(email, String.format(
                        "Alert: You have exceeded your monthly budget for %s! Spent: $%.2f / Limit: $%.2f",
                        ym, usage.getTotalSpent(), usage.getLimit()
                    ));
                } else if (pct >= 80.0) {
                    notificationService.sendNotification(email, String.format(
                        "Notice: You have used %.1f%% of your monthly budget for %s. Current Spent: $%.2f / Limit: $%.2f",
                        pct, ym, usage.getTotalSpent(), usage.getLimit()
                    ));
                }
            }
        } catch (Exception ex) {
            // Keep app functioning even if notification fails
        }
    }

    public ExpenseDto getExpenseById(Long id, String email) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        if (!expense.getUser().getEmail().equals(email)) {
            throw new BadRequestException("Access denied to this expense record.");
        }

        return expenseMapper.toDto(expense);
    }

    public ExpenseDto updateExpense(Long id, ExpenseDto dto, String email) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        if (!expense.getUser().getEmail().equals(email)) {
            throw new BadRequestException("Access denied to update this expense record.");
        }

        Category category = categoryRepository.findByName(dto.getCategoryName())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryName()));

        expense.setTitle(dto.getTitle());
        expense.setAmount(dto.getAmount());
        expense.setCategory(category);
        expense.setDate(dto.getDate());
        expense.setPaymentMethod(dto.getPaymentMethod());
        expense.setDescription(dto.getDescription());

        Expense saved = expenseRepository.save(expense);

        // Trigger real-time budget check
        checkBudgetAndNotify(email, saved.getDate());

        return expenseMapper.toDto(saved);
    }

    public void deleteExpense(Long id, String email) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        if (!expense.getUser().getEmail().equals(email)) {
            throw new BadRequestException("Access denied to delete this expense record.");
        }

        expenseRepository.delete(expense);
    }

    public List<ExpenseDto> getExpensesFiltered(String email, String categoryName, LocalDate startDate, LocalDate endDate, String search, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Expense> expenses = expenseRepository.findByUser(user);

        return expenses.stream()
                .filter(e -> categoryName == null || e.getCategory().getName().equalsIgnoreCase(categoryName))
                .filter(e -> startDate == null || !e.getDate().isBefore(startDate))
                .filter(e -> endDate == null || !e.getDate().isAfter(endDate))
                .filter(e -> search == null || e.getTitle().toLowerCase().contains(search.toLowerCase()) || 
                             (e.getDescription() != null && e.getDescription().toLowerCase().contains(search.toLowerCase())))
                .sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate())) // Sorted newest first
                .skip((long) page * size)
                .limit(size)
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
    }
}

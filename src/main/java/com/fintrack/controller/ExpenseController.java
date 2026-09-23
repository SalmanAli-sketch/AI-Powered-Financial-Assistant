package com.fintrack.controller;

import com.fintrack.dto.ExpenseDto;
import com.fintrack.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(@Valid @RequestBody ExpenseDto dto, Principal principal) {
        ExpenseDto created = expenseService.createExpense(dto, principal.getName());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpenseById(@PathVariable Long id, Principal principal) {
        ExpenseDto expense = expenseService.getExpenseById(id, principal.getName());
        return ResponseEntity.ok(expense);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseDto dto, Principal principal) {
        ExpenseDto updated = expenseService.updateExpense(id, dto, principal.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id, Principal principal) {
        expenseService.deleteExpense(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        List<ExpenseDto> expenses = expenseService.getExpensesFiltered(
                principal.getName(), category, startDate, endDate, search, page, size
        );
        return ResponseEntity.ok(expenses);
    }
}

package com.fintrack.controller;

import com.fintrack.dto.BudgetDto;
import com.fintrack.dto.BudgetUsageResponse;
import com.fintrack.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin(origins = "*")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetDto> setBudget(@Valid @RequestBody BudgetDto dto, Principal principal) {
        BudgetDto created = budgetService.setBudget(dto, principal.getName());
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<BudgetDto> getBudget(@RequestParam String month, Principal principal) {
        YearMonth ym = YearMonth.parse(month);
        BudgetDto budget = budgetService.getBudget(ym, principal.getName());
        return ResponseEntity.ok(budget);
    }

    @GetMapping("/usage")
    public ResponseEntity<BudgetUsageResponse> getBudgetUsage(@RequestParam(required = false) String month, Principal principal) {
        YearMonth ym = (month != null) ? YearMonth.parse(month) : YearMonth.now();
        BudgetUsageResponse usage = budgetService.getBudgetUsage(ym, principal.getName());
        return ResponseEntity.ok(usage);
    }
}

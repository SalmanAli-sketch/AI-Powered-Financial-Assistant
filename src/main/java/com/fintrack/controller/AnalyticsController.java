package com.fintrack.controller;

import com.fintrack.dto.AnalyticsSummaryResponse;
import com.fintrack.dto.CategoryBreakdownDto;
import com.fintrack.dto.MonthlyTrendDto;
import com.fintrack.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryResponse> getMonthlySummary(
            @RequestParam(required = false) String month,
            Principal principal) {
        YearMonth ym = (month != null) ? YearMonth.parse(month) : YearMonth.now();
        AnalyticsSummaryResponse summary = analyticsService.getMonthlySummary(ym, principal.getName());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/breakdown")
    public ResponseEntity<List<CategoryBreakdownDto>> getCategoryBreakdown(
            @RequestParam(required = false) String month,
            Principal principal) {
        YearMonth ym = (month != null) ? YearMonth.parse(month) : YearMonth.now();
        List<CategoryBreakdownDto> breakdown = analyticsService.getCategoryBreakdown(ym, principal.getName());
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/trends")
    public ResponseEntity<List<MonthlyTrendDto>> getSpendingTrends(
            @RequestParam(defaultValue = "6") int limit,
            Principal principal) {
        List<MonthlyTrendDto> trends = analyticsService.getSpendingTrends(limit, principal.getName());
        return ResponseEntity.ok(trends);
    }
}

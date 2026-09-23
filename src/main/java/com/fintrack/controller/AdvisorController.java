package com.fintrack.controller;

import com.fintrack.dto.SuggestionResponse;
import com.fintrack.service.AdvisorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AdvisorController {

    private final AdvisorService advisorService;

    public AdvisorController(AdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @GetMapping("/suggestions")
    public ResponseEntity<SuggestionResponse> getSuggestions(Principal principal) {
        SuggestionResponse suggestions = advisorService.getSuggestions(principal.getName());
        return ResponseEntity.ok(suggestions);
    }
}

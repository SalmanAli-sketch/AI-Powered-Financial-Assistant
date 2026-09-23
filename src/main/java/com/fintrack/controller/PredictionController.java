package com.fintrack.controller;

import com.fintrack.dto.PredictionResponse;
import com.fintrack.service.PredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping("/projection")
    public ResponseEntity<PredictionResponse> getProjection(Principal principal) {
        PredictionResponse prediction = predictionService.getSpendingProjection(principal.getName());
        return ResponseEntity.ok(prediction);
    }
}

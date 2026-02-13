package com.roulette.predictor.controller;

import com.roulette.predictor.dto.AnalysisRequest;
import com.roulette.predictor.dto.AnalysisResponse;
import com.roulette.predictor.service.PatternAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.roulette.predictor.service.DozenAnalysisService;     
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/roulette")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class RouletteController {

    private final PatternAnalysisService patternAnalysisService;
    private final DozenAnalysisService dozenAnalysisService;

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeAndPredict(
            @Valid @RequestBody AnalysisRequest request) {
        
        log.info("Received analysis request with {} numbers", 
                request.getHistoricalNumbers().size());

        AnalysisResponse response = patternAnalysisService
                .analyzePatternsAndPredict(request.getHistoricalNumbers());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze-dozens")
    public ResponseEntity<Map<String, Object>> analyzeDozenPatterns(
            @Valid @RequestBody AnalysisRequest request) {
        
        log.info("Received dozen analysis request with {} numbers", 
                request.getHistoricalNumbers().size());

        Map<String, Object> response = dozenAnalysisService
                .analyzeDozenPatterns(request.getHistoricalNumbers());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Roulette Predictor API is running!");
    }
}

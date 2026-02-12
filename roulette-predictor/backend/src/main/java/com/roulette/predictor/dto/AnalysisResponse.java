package com.roulette.predictor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {
    private List<Prediction> topPredictions;
    private Map<String, Object> statistics;
    private List<String> patternsDetected;
    private long timestamp;
}

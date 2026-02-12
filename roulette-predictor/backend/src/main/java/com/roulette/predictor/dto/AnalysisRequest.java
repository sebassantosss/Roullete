package com.roulette.predictor.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    @NotNull(message = "Historical numbers cannot be null")
    @Size(min = 5, message = "At least 5 historical numbers required")
    private List<Integer> historicalNumbers;
}

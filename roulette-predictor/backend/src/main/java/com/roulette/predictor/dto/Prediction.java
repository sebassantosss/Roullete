package com.roulette.predictor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prediction {
    private Integer number;
    private Double probability;
    private Integer confirmations;
    private List<String> reasons;
    private String patternType;
}

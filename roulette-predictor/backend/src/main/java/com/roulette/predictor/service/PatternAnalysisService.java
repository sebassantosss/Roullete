package com.roulette.predictor.service;

import com.roulette.predictor.dto.AnalysisResponse;
import com.roulette.predictor.dto.Prediction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PatternAnalysisService {

    private static final int MAX_ROULETTE_NUMBER = 36;
    private static final int TOP_PREDICTIONS_COUNT = 8;

    public AnalysisResponse analyzePatternsAndPredict(List<Integer> historicalNumbers) {
        log.info("Analyzing {} historical numbers", historicalNumbers.size());

        Map<Integer, List<String>> confirmations = new HashMap<>();
        
        // Initialize confirmations map
        for (int i = 0; i <= MAX_ROULETTE_NUMBER; i++) {
            confirmations.put(i, new ArrayList<>());
        }

        // Analyze all patterns
        analyzeDigitSumPattern(historicalNumbers, confirmations);
        analyzeMathematicalOperations(historicalNumbers, confirmations);
        analyzeSequencePattern(historicalNumbers, confirmations);
        analyzeConsecutiveDifferences(historicalNumbers, confirmations);
        analyzePositionPattern(historicalNumbers, confirmations);
        analyzeDoublesPattern(historicalNumbers, confirmations);
        analyzeDigitInversion(historicalNumbers, confirmations);
        analyzeHotNumbers(historicalNumbers, confirmations);

        // Build predictions
        List<Prediction> predictions = buildPredictions(confirmations, historicalNumbers);

        // Calculate statistics
        Map<String, Object> statistics = calculateStatistics(historicalNumbers);

        // Detect patterns
        List<String> patternsDetected = detectActivePatterns(historicalNumbers);

        return AnalysisResponse.builder()
                .topPredictions(predictions)
                .statistics(statistics)
                .patternsDetected(patternsDetected)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * PATTERN 1: Digit Sum Pattern (Most successful)
     * Example: 23 -> 2+3=5, then 5 appears
     */
    private void analyzeDigitSumPattern(List<Integer> numbers, Map<Integer, List<String>> confirmations) {
        Map<Integer, Integer> numberFrequency = getNumberFrequency(numbers);
        
        for (Map.Entry<Integer, Integer> entry : numberFrequency.entrySet()) {
            int num = entry.getKey();
            int frequency = entry.getValue();
            int digitSum = getDigitSum(num);
            
            if (digitSum <= MAX_ROULETTE_NUMBER) {
                String reason = String.format("Suma de dígitos de %d (×%d) = %d", num, frequency, digitSum);
                confirmations.get(digitSum).add(reason);
            }
        }
    }

    /**
     * PATTERN 2: Mathematical Operations
     * Multiplication, division, addition, subtraction
     */
    private void analyzeMathematicalOperations(List<Integer> numbers, Map<Integer, List<String>> confirmations) {
        if (numbers.isEmpty()) return;
        
        int lastNumber = numbers.get(numbers.size() - 1);
        Map<Integer, Integer> frequency = getNumberFrequency(numbers);

        // Multiplications and divisions with frequent numbers
        for (Map.Entry<Integer, Integer> entry : frequency.entrySet()) {
            int baseNum = entry.getKey();
            int freq = entry.getValue();
            
            if (baseNum == 0) continue;

            // Multiplications
            for (int multiplier = 2; multiplier <= 5; multiplier++) {
                int result = baseNum * multiplier;
                if (result <= MAX_ROULETTE_NUMBER) {
                    String reason = String.format("%d × %d = %d (base apareció %dx)", 
                            baseNum, multiplier, result, freq);
                    confirmations.get(result).add(reason);
                }
            }

            // Divisions
            for (int divisor = 2; divisor <= 9; divisor++) {
                if (baseNum % divisor == 0) {
                    int result = baseNum / divisor;
                    if (result <= MAX_ROULETTE_NUMBER) {
                        String reason = String.format("%d ÷ %d = %d", baseNum, divisor, result);
                        confirmations.get(result).add(reason);
                    }
                }
            }
        }

        // Operations with last number
        for (int i = 1; i <= 20; i++) {
            // Additions
            if (lastNumber + i <= MAX_ROULETTE_NUMBER) {
                String reason = String.format("%d + %d = %d (último número)", 
                        lastNumber, i, lastNumber + i);
                confirmations.get(lastNumber + i).add(reason);
            }

            // Subtractions
            if (lastNumber - i >= 0 && lastNumber - i <= MAX_ROULETTE_NUMBER) {
                String reason = String.format("%d - %d = %d (último número)", 
                        lastNumber, i, lastNumber - i);
                confirmations.get(lastNumber - i).add(reason);
            }
        }
    }

    /**
     * PATTERN 3: Sequence Pattern (12-24-36)
     */
    private void analyzeSequencePattern(List<Integer> numbers, Map<Integer, List<String>> confirmations) {
        Map<Integer, Integer> frequency = getNumberFrequency(numbers);

        if (frequency.getOrDefault(12, 0) > 0) {
            confirmations.get(24).add("Patrón 12-24-36: 12×2 = 24");
            confirmations.get(36).add("Patrón 12-24-36: 12×3 = 36");
        }

        if (frequency.getOrDefault(24, 0) > 0) {
            confirmations.get(12).add("Patrón 12-24-36: 24÷2 = 12");
            confirmations.get(36).add("Patrón 12-24-36: 24+12 = 36");
        }
    }

    /**
     * PATTERN 4: Consecutive Differences
     */
    private void analyzeConsecutiveDifferences(List<Integer> numbers, Map<Integer, List<String>> confirmations) {
        if (numbers.size() < 2) return;

        for (int i = numbers.size() - 1; i >= Math.max(0, numbers.size() - 5); i--) {
            int current = numbers.get(i);
            
            for (int j = i - 1; j >= Math.max(0, i - 3); j--) {
                int previous = numbers.get(j);
                int diff = Math.abs(current - previous);
                
                if (diff > 0 && diff <= MAX_ROULETTE_NUMBER) {
                    String reason = String.format("Diferencia: %d - %d = %d", 
                            Math.max(current, previous), Math.min(current, previous), diff);
                    confirmations.get(diff).add(reason);
                }
            }
        }
    }

    /**
     * PATTERN 5: Position = Number
     * Example: Number 27 appeared at position 27
     */
    private void analyzePositionPattern(List<Integer> numbers, Map<Integer, List<String>> confirmations) {
        int nextPosition = numbers.size() + 1;
        
        if (nextPosition <= MAX_ROULETTE_NUMBER) {
            confirmations.get(nextPosition).add(
                    String.format("Patrón posición = número (posición %d)", nextPosition)
            );
        }
    }

    /**
     * PATTERN 6: Doubles Pattern (11, 22, 33, 00)
     */
    private void analyzeDoublesPattern(List<Integer> numbers, Map<Integer, List<String>> confirmations) {
        Map<Integer, Integer> frequency = getNumberFrequency(numbers);

        int[] doubles = {11, 22, 33};
        int doublesCount = 0;
        
        for (int d : doubles) {
            if (frequency.getOrDefault(d, 0) > 0) {
                doublesCount++;
            }
        }

        if (doublesCount >= 2) {
            confirmations.get(0).add("Patrón de dobles: 11, 22, 33 → siguiente 00");
            for (int d : doubles) {
                if (frequency.getOrDefault(d, 0) == 0) {
                    confirmations.get(d).add("Completar patrón de dobles");
                }
            }
        }
    }

    /**
     * PATTERN 7: Digit Inversion
     * Example: 23 ↔ 32
     */
    private void analyzeDigitInversion(List<Integer> numbers, Map<Integer, List<String>> confirmations) {
        for (int num : numbers) {
            if (num >= 10) {
                int tens = num / 10;
                int ones = num % 10;
                int inverted = ones * 10 + tens;
                
                if (inverted <= MAX_ROULETTE_NUMBER && inverted != num) {
                    confirmations.get(inverted).add(
                            String.format("Inversión de dígitos: %d ↔ %d", num, inverted)
                    );
                }
            }
        }
    }

    /**
     * PATTERN 8: Hot Numbers (that repeat frequently)
     */
    private void analyzeHotNumbers(List<Integer> numbers, Map<Integer, List<String>> confirmations) {
        Map<Integer, Integer> frequency = getNumberFrequency(numbers);

        List<Map.Entry<Integer, Integer>> sorted = frequency.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        for (Map.Entry<Integer, Integer> entry : sorted) {
            if (entry.getValue() >= 2) {
                confirmations.get(entry.getKey()).add(
                        String.format("Número caliente (salió %d veces)", entry.getValue())
                );
            }
        }
    }

    /**
     * Build final predictions with probabilities
     */
    private List<Prediction> buildPredictions(Map<Integer, List<String>> confirmations, 
                                               List<Integer> historicalNumbers) {
        List<Prediction> predictions = new ArrayList<>();

        // Get last 10 numbers for recent appearance penalty
        int recentWindow = Math.min(10, historicalNumbers.size());
        List<Integer> recentNumbers = historicalNumbers.subList(
                Math.max(0, historicalNumbers.size() - recentWindow), 
                historicalNumbers.size()
        );
        
        // Count how many times each number appeared recently
        Map<Integer, Integer> recentFrequency = new HashMap<>();
        for (int num : recentNumbers) {
            recentFrequency.put(num, recentFrequency.getOrDefault(num, 0) + 1);
        }

        for (Map.Entry<Integer, List<String>> entry : confirmations.entrySet()) {
            int number = entry.getKey();
            List<String> reasons = entry.getValue();
            
            if (!reasons.isEmpty()) {
                int confirmationCount = reasons.size();
                
                // Calculate base probability
                double baseProbability = Math.min(confirmationCount * 5.0, 80.0);
                
                // Apply penalties for recent appearances
                double probability = baseProbability;
                
                // CRITICAL: Heavy penalty for numbers that appeared recently
                int recentAppearances = recentFrequency.getOrDefault(number, 0);
                
                if (recentAppearances > 0) {
                    // Penalty increases exponentially with recent appearances
                    // Last 1 appearance: 70% penalty
                    // Last 2 appearances: 85% penalty
                    // Last 3+ appearances: 95% penalty
                    double penaltyFactor = 1.0 - (0.3 + (recentAppearances - 1) * 0.15);
                    penaltyFactor = Math.max(penaltyFactor, 0.05); // Min 5% of original
                    probability *= penaltyFactor;
                    
                    // Also reduce confirmation count display for recently appeared
                    confirmationCount = (int) Math.ceil(confirmationCount * penaltyFactor);
                }

                predictions.add(Prediction.builder()
                        .number(number)
                        .probability(Math.round(probability * 10.0) / 10.0)
                        .confirmations(confirmationCount)
                        .reasons(reasons)
                        .patternType(determineMainPattern(reasons))
                        .build());
            }
        }

        // Sort by probability descending
        predictions.sort(Comparator.comparingDouble(Prediction::getProbability).reversed());

        return predictions.stream()
                .limit(TOP_PREDICTIONS_COUNT)
                .collect(Collectors.toList());
    }

    private String determineMainPattern(List<String> reasons) {
        if (reasons.stream().anyMatch(r -> r.contains("Suma de dígitos"))) {
            return "DIGIT_SUM";
        } else if (reasons.stream().anyMatch(r -> r.contains("×") || r.contains("÷"))) {
            return "MATHEMATICAL";
        } else if (reasons.stream().anyMatch(r -> r.contains("Diferencia"))) {
            return "DIFFERENCE";
        } else if (reasons.stream().anyMatch(r -> r.contains("caliente"))) {
            return "HOT_NUMBER";
        }
        return "MIXED";
    }

    private Map<String, Object> calculateStatistics(List<Integer> numbers) {
        Map<String, Object> stats = new HashMap<>();
        Map<Integer, Integer> frequency = getNumberFrequency(numbers);

        // Most frequent numbers
        List<Map.Entry<Integer, Integer>> topFrequent = frequency.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        stats.put("totalNumbers", numbers.size());
        stats.put("uniqueNumbers", frequency.size());
        stats.put("mostFrequent", topFrequent);
        stats.put("lastNumber", numbers.isEmpty() ? null : numbers.get(numbers.size() - 1));

        // Recently appeared numbers (last 10)
        int recentWindow = Math.min(10, numbers.size());
        List<Integer> recentNumbers = numbers.subList(
                Math.max(0, numbers.size() - recentWindow), 
                numbers.size()
        );
        stats.put("recentNumbers", recentNumbers);
        
        // Numbers that appeared in last 5 (should be penalized)
        Set<Integer> last5 = new HashSet<>(numbers.subList(
                Math.max(0, numbers.size() - 5), 
                numbers.size()
        ));
        stats.put("recentlyAppeared", last5);

        // Dozen distribution
        long first = numbers.stream().filter(n -> n >= 1 && n <= 12).count();
        long second = numbers.stream().filter(n -> n >= 13 && n <= 24).count();
        long third = numbers.stream().filter(n -> n >= 25 && n <= 36).count();
        
        Map<String, Long> dozens = new HashMap<>();
        dozens.put("1st", first);
        dozens.put("2nd", second);
        dozens.put("3rd", third);
        stats.put("dozenDistribution", dozens);

        return stats;
    }

    private List<String> detectActivePatterns(List<Integer> numbers) {
        List<String> patterns = new ArrayList<>();
        Map<Integer, Integer> frequency = getNumberFrequency(numbers);

        // Check for digit sum pattern
        boolean hasDigitSumPattern = numbers.stream()
                .anyMatch(n -> {
                    int sum = getDigitSum(n);
                    return frequency.getOrDefault(sum, 0) > 0;
                });
        if (hasDigitSumPattern) patterns.add("Patrón de suma de dígitos activo");

        // Check for 12-24-36 pattern
        if (frequency.containsKey(12) && frequency.containsKey(24)) {
            patterns.add("Patrón 12-24-36 detectado");
        }

        // Check for doubles
        int doublesCount = (frequency.getOrDefault(11, 0) > 0 ? 1 : 0) +
                          (frequency.getOrDefault(22, 0) > 0 ? 1 : 0) +
                          (frequency.getOrDefault(33, 0) > 0 ? 1 : 0);
        if (doublesCount >= 2) {
            patterns.add("Patrón de dobles (11, 22, 33) activo");
        }

        // Check for hot numbers
        long hotNumbers = frequency.values().stream().filter(count -> count >= 3).count();
        if (hotNumbers > 0) {
            patterns.add(String.format("%d números calientes detectados (≥3 apariciones)", hotNumbers));
        }

        return patterns;
    }

    private Map<Integer, Integer> getNumberFrequency(List<Integer> numbers) {
        Map<Integer, Integer> frequency = new HashMap<>();
        for (int num : numbers) {
            frequency.put(num, frequency.getOrDefault(num, 0) + 1);
        }
        return frequency;
    }

    private int getDigitSum(int number) {
        int sum = 0;
        while (number > 0) {
            sum += number % 10;
            number /= 10;
        }
        return sum;
    }
}

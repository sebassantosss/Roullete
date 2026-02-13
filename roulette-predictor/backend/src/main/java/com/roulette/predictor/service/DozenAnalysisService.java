package com.roulette.predictor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DozenAnalysisService {

    /**
     * Analyze dozen patterns and predict next dozen probabilities
     */
    public Map<String, Object> analyzeDozenPatterns(List<Integer> numbers) {
        Map<String, Object> analysis = new HashMap<>();
        
        if (numbers.isEmpty()) {
            return analysis;
        }

        // Convert numbers to dozens
        List<Integer> dozenSequence = numbers.stream()
                .map(this::getDozen)
                .collect(Collectors.toList());

        // 1. Basic dozen statistics
        Map<String, Object> statistics = calculateDozenStatistics(dozenSequence);
        analysis.put("statistics", statistics);

        // 2. Transition patterns (which dozen follows which)
        Map<String, Object> transitions = analyzeTransitions(dozenSequence);
        analysis.put("transitions", transitions);

        // 3. Streak analysis (hot/cold dozens)
        Map<String, Object> streaks = analyzeStreaks(dozenSequence);
        analysis.put("streaks", streaks);

        // 4. Number-to-dozen patterns (specific numbers that lead to specific dozens)
        Map<String, Object> numberPatterns = analyzeNumberToDozenPatterns(numbers);
        analysis.put("numberPatterns", numberPatterns);

        // 5. Sequence patterns (last 3-5 dozens predict next)
        Map<String, Object> sequencePatterns = analyzeSequencePatterns(dozenSequence);
        analysis.put("sequencePatterns", sequencePatterns);

        // 6. FINAL PREDICTION - Calculate probabilities for each dozen
        Map<String, Double> predictions = calculateDozenProbabilities(
                numbers, dozenSequence, transitions, streaks, numberPatterns, sequencePatterns
        );
        analysis.put("predictions", predictions);

        // 7. Confidence level
        analysis.put("confidence", calculateConfidenceLevel(predictions));

        return analysis;
    }

    /**
     * Get dozen for a number (0 returns 0, 1-12 = 1st, 13-24 = 2nd, 25-36 = 3rd)
     */
    private int getDozen(int number) {
        if (number == 0) return 0;
        if (number >= 1 && number <= 12) return 1;
        if (number >= 13 && number <= 24) return 2;
        if (number >= 25 && number <= 36) return 3;
        return 0;
    }

    private String getDozenName(int dozen) {
        switch (dozen) {
            case 1: return "1st";
            case 2: return "2nd";
            case 3: return "3rd";
            case 0: return "zero";
            default: return "unknown";
        }
    }

    /**
     * Calculate basic dozen statistics
     */
    private Map<String, Object> calculateDozenStatistics(List<Integer> dozenSequence) {
        Map<String, Object> stats = new HashMap<>();
        
        Map<Integer, Long> frequency = dozenSequence.stream()
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()));

        stats.put("total", dozenSequence.size());
        stats.put("1st_count", frequency.getOrDefault(1, 0L));
        stats.put("2nd_count", frequency.getOrDefault(2, 0L));
        stats.put("3rd_count", frequency.getOrDefault(3, 0L));
        stats.put("zero_count", frequency.getOrDefault(0, 0L));

        // Percentages
        int total = dozenSequence.size();
        stats.put("1st_percent", (frequency.getOrDefault(1, 0L) * 100.0) / total);
        stats.put("2nd_percent", (frequency.getOrDefault(2, 0L) * 100.0) / total);
        stats.put("3rd_percent", (frequency.getOrDefault(3, 0L) * 100.0) / total);

        // Last dozen
        if (!dozenSequence.isEmpty()) {
            stats.put("lastDozen", dozenSequence.get(dozenSequence.size() - 1));
        }

        return stats;
    }

    /**
     * Analyze transition patterns (which dozen follows which)
     * Example: 1st → 3rd happens 15 times, 2nd → 1st happens 8 times
     */
    private Map<String, Object> analyzeTransitions(List<Integer> dozenSequence) {
        Map<String, Object> transitions = new HashMap<>();
        Map<String, Integer> transitionCounts = new HashMap<>();

        for (int i = 0; i < dozenSequence.size() - 1; i++) {
            int from = dozenSequence.get(i);
            int to = dozenSequence.get(i + 1);
            String key = getDozenName(from) + "→" + getDozenName(to);
            transitionCounts.put(key, transitionCounts.getOrDefault(key, 0) + 1);
        }

        transitions.put("counts", transitionCounts);

        // Most common transitions
        List<Map.Entry<String, Integer>> sorted = transitionCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
        transitions.put("topTransitions", sorted);

        // Predict based on last dozen
        if (!dozenSequence.isEmpty()) {
            int lastDozen = dozenSequence.get(dozenSequence.size() - 1);
            Map<Integer, Integer> nextDozenCounts = new HashMap<>();
            
            for (int i = 0; i < dozenSequence.size() - 1; i++) {
                if (dozenSequence.get(i) == lastDozen) {
                    int next = dozenSequence.get(i + 1);
                    nextDozenCounts.put(next, nextDozenCounts.getOrDefault(next, 0) + 1);
                }
            }

            transitions.put("afterLast", nextDozenCounts);
        }

        return transitions;
    }

    /**
     * Analyze streaks (consecutive appearances of same dozen)
     */
    private Map<String, Object> analyzeStreaks(List<Integer> dozenSequence) {
        Map<String, Object> streaks = new HashMap<>();
        
        // Current streak
        int currentStreak = 1;
        int lastDozen = dozenSequence.isEmpty() ? -1 : dozenSequence.get(dozenSequence.size() - 1);
        
        for (int i = dozenSequence.size() - 2; i >= 0 && dozenSequence.get(i) == lastDozen; i--) {
            currentStreak++;
        }

        streaks.put("currentStreak", currentStreak);
        streaks.put("currentDozen", lastDozen);

        // Find all streaks of 3+
        List<Map<String, Object>> longStreaks = new ArrayList<>();
        int i = 0;
        while (i < dozenSequence.size()) {
            int dozen = dozenSequence.get(i);
            int streakLength = 1;
            while (i + streakLength < dozenSequence.size() && 
                   dozenSequence.get(i + streakLength) == dozen) {
                streakLength++;
            }
            
            if (streakLength >= 3) {
                Map<String, Object> streak = new HashMap<>();
                streak.put("dozen", getDozenName(dozen));
                streak.put("length", streakLength);
                streak.put("position", i);
                longStreaks.add(streak);
            }
            
            i += streakLength;
        }

        streaks.put("longStreaks", longStreaks);

        // Hot/Cold analysis (last 20 spins)
        int window = Math.min(20, dozenSequence.size());
        List<Integer> recent = dozenSequence.subList(dozenSequence.size() - window, dozenSequence.size());
        Map<Integer, Long> recentFreq = recent.stream()
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()));

        Map<String, Long> hotCold = new HashMap<>();
        hotCold.put("1st", recentFreq.getOrDefault(1, 0L));
        hotCold.put("2nd", recentFreq.getOrDefault(2, 0L));
        hotCold.put("3rd", recentFreq.getOrDefault(3, 0L));

        streaks.put("last20", hotCold);

        return streaks;
    }

    /**
     * Analyze which specific numbers lead to which dozens
     * Example: After number 4, 3rd dozen appears 70% of the time
     */
    private Map<String, Object> analyzeNumberToDozenPatterns(List<Integer> numbers) {
        Map<String, Object> patterns = new HashMap<>();
        Map<Integer, Map<Integer, Integer>> numberToDozenMap = new HashMap<>();

        for (int i = 0; i < numbers.size() - 1; i++) {
            int currentNum = numbers.get(i);
            int nextDozen = getDozen(numbers.get(i + 1));
            
            numberToDozenMap.putIfAbsent(currentNum, new HashMap<>());
            Map<Integer, Integer> dozenCounts = numberToDozenMap.get(currentNum);
            dozenCounts.put(nextDozen, dozenCounts.getOrDefault(nextDozen, 0) + 1);
        }

        // Find strong patterns (numbers that lead to specific dozens >60% of time)
        List<Map<String, Object>> strongPatterns = new ArrayList<>();
        
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : numberToDozenMap.entrySet()) {
            int number = entry.getKey();
            Map<Integer, Integer> dozenCounts = entry.getValue();
            int total = dozenCounts.values().stream().mapToInt(Integer::intValue).sum();
            
            if (total >= 3) { // At least 3 occurrences
                for (Map.Entry<Integer, Integer> dozenEntry : dozenCounts.entrySet()) {
                    double percentage = (dozenEntry.getValue() * 100.0) / total;
                    if (percentage >= 50) { // Strong pattern
                        Map<String, Object> pattern = new HashMap<>();
                        pattern.put("number", number);
                        pattern.put("dozen", getDozenName(dozenEntry.getKey()));
                        pattern.put("occurrences", dozenEntry.getValue());
                        pattern.put("total", total);
                        pattern.put("percentage", Math.round(percentage * 10) / 10.0);
                        strongPatterns.add(pattern);
                    }
                }
            }
        }

        strongPatterns.sort((a, b) -> 
            Double.compare((Double)b.get("percentage"), (Double)a.get("percentage"))
        );

        patterns.put("strongPatterns", strongPatterns);
        
        // Current number prediction
        if (!numbers.isEmpty()) {
            int lastNumber = numbers.get(numbers.size() - 1);
            if (numberToDozenMap.containsKey(lastNumber)) {
                patterns.put("afterLastNumber", numberToDozenMap.get(lastNumber));
            }
        }

        return patterns;
    }

    /**
     * Analyze sequence patterns (last 3-5 dozens)
     * Example: Pattern [1st, 2nd, 1st] → usually 3rd comes next
     */
    private Map<String, Object> analyzeSequencePatterns(List<Integer> dozenSequence) {
        Map<String, Object> patterns = new HashMap<>();
        Map<String, Map<Integer, Integer>> sequenceMap = new HashMap<>();

        // Analyze 3-dozen sequences
        for (int i = 0; i < dozenSequence.size() - 3; i++) {
            String sequence = dozenSequence.get(i) + "," + 
                            dozenSequence.get(i+1) + "," + 
                            dozenSequence.get(i+2);
            int next = dozenSequence.get(i + 3);
            
            sequenceMap.putIfAbsent(sequence, new HashMap<>());
            Map<Integer, Integer> nextCounts = sequenceMap.get(sequence);
            nextCounts.put(next, nextCounts.getOrDefault(next, 0) + 1);
        }

        // Current sequence prediction
        if (dozenSequence.size() >= 3) {
            int size = dozenSequence.size();
            String currentSeq = dozenSequence.get(size-3) + "," + 
                              dozenSequence.get(size-2) + "," + 
                              dozenSequence.get(size-1);
            
            if (sequenceMap.containsKey(currentSeq)) {
                patterns.put("currentSequence", currentSeq);
                patterns.put("predictions", sequenceMap.get(currentSeq));
            }
        }

        return patterns;
    }

    /**
     * Calculate final dozen probabilities based on all patterns
     */
    private Map<String, Double> calculateDozenProbabilities(
            List<Integer> numbers,
            List<Integer> dozenSequence,
            Map<String, Object> transitions,
            Map<String, Object> streaks,
            Map<String, Object> numberPatterns,
            Map<String, Object> sequencePatterns) {

        Map<String, Double> scores = new HashMap<>();
        scores.put("1st", 0.0);
        scores.put("2nd", 0.0);
        scores.put("3rd", 0.0);

        int totalWeight = 0;

        // Weight 1: Transition patterns (30% weight)
        if (transitions.containsKey("afterLast")) {
            Map<Integer, Integer> afterLast = (Map<Integer, Integer>) transitions.get("afterLast");
            int total = afterLast.values().stream().mapToInt(Integer::intValue).sum();
            for (Map.Entry<Integer, Integer> entry : afterLast.entrySet()) {
                String dozenName = getDozenName(entry.getKey());
                if (scores.containsKey(dozenName)) {
                    scores.put(dozenName, scores.get(dozenName) + (entry.getValue() * 30.0) / total);
                }
            }
            totalWeight += 30;
        }

        // Weight 2: Hot/Cold analysis (25% weight)
        if (streaks.containsKey("last20")) {
            Map<String, Long> last20 = (Map<String, Long>) streaks.get("last20");
            long total = last20.values().stream().mapToLong(Long::longValue).sum();
            if (total > 0) {
                for (Map.Entry<String, Long> entry : last20.entrySet()) {
                    scores.put(entry.getKey(), scores.get(entry.getKey()) + (entry.getValue() * 25.0) / total);
                }
                totalWeight += 25;
            }
        }

        // Weight 3: Number-to-dozen patterns (25% weight)
        if (numberPatterns.containsKey("afterLastNumber")) {
            Map<Integer, Integer> afterLastNum = (Map<Integer, Integer>) numberPatterns.get("afterLastNumber");
            int total = afterLastNum.values().stream().mapToInt(Integer::intValue).sum();
            for (Map.Entry<Integer, Integer> entry : afterLastNum.entrySet()) {
                String dozenName = getDozenName(entry.getKey());
                if (scores.containsKey(dozenName)) {
                    scores.put(dozenName, scores.get(dozenName) + (entry.getValue() * 25.0) / total);
                }
            }
            totalWeight += 25;
        }

        // Weight 4: Sequence patterns (20% weight)
        if (sequencePatterns.containsKey("predictions")) {
            Map<Integer, Integer> seqPred = (Map<Integer, Integer>) sequencePatterns.get("predictions");
            int total = seqPred.values().stream().mapToInt(Integer::intValue).sum();
            for (Map.Entry<Integer, Integer> entry : seqPred.entrySet()) {
                String dozenName = getDozenName(entry.getKey());
                if (scores.containsKey(dozenName)) {
                    scores.put(dozenName, scores.get(dozenName) + (entry.getValue() * 20.0) / total);
                }
            }
            totalWeight += 20;
        }

        // Normalize to 100%
        if (totalWeight > 0) {
            double factor = 100.0 / totalWeight;
            for (String key : scores.keySet()) {
                scores.put(key, Math.round(scores.get(key) * factor * 10) / 10.0);
            }
        } else {
            // Default equal probabilities
            scores.put("1st", 33.3);
            scores.put("2nd", 33.3);
            scores.put("3rd", 33.4);
        }

        return scores;
    }

    private String calculateConfidenceLevel(Map<String, Double> predictions) {
        // Get top 2 dozens
        List<Double> sortedProbs = predictions.values().stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        
        if (sortedProbs.size() < 2) {
            return "LOW";
        }
        
        double top1 = sortedProbs.get(0);
        double top2 = sortedProbs.get(1);
        double bottom = sortedProbs.get(2);
        
        // Sum of top 2 dozens
        double combinedProb = top1 + top2;
        
        // Spread between top 2 and bottom
        double avgTop2 = combinedProb / 2;
        double spread = avgTop2 - bottom;

        // Confidence based on:
        // 1. Combined probability of top 2
        // 2. Separation from the third dozen
        if (combinedProb >= 70 && spread >= 15) return "VERY_HIGH";
        if (combinedProb >= 65 && spread >= 10) return "HIGH";
        if (combinedProb >= 60 && spread >= 5) return "MEDIUM";
        return "LOW";
    }
}

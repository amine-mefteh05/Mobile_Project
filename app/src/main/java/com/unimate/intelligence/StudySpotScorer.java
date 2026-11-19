package com.unimate.intelligence;

import com.unimate.dto.ScoredPlace;
import com.unimate.dto.Weights;
import com.unimate.model.StudyPlace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudySpotScorer {

    public static ScoredPlace scorePlace(StudyPlace place, Weights weights, boolean isBudgetTight) {
        if (place == null) {
            return new ScoredPlace(null, 0.0, "Invalid place");
        }

        int quietW = weights != null ? weights.getQuietWeight() : 50;
        int wifiW = weights != null ? weights.getWifiWeight() : 50;
        int budgetW = weights != null ? weights.getBudgetWeight() : 50;
        int distanceW = weights != null ? weights.getDistanceWeight() : 50;

        // Tight Budget Rule: double the budget weight before normalizing!
        if (isBudgetTight) {
            budgetW *= 2;
        }

        double totalWeight = quietW + wifiW + budgetW + distanceW;
        if (totalWeight <= 0) totalWeight = 1.0;

        // Individual component scores (0 to 100)
        double quietScore = Math.max(0, (6 - place.getNoiseLevel()) * 20.0);
        double wifiScore = place.isWifi() ? 100.0 : 0.0;
        double budgetScore = Math.max(0, (4 - place.getPriceLevel()) * 33.33);
        double distanceScore = 85.0; // Standard score for local campus radius

        double weightedSum = (quietScore * quietW) + (wifiScore * wifiW)
                + (budgetScore * budgetW) + (distanceScore * distanceW);

        double finalScore = Math.min(100.0, Math.max(0.0, weightedSum / totalWeight));

        String breakdown = String.format("Score: %.1f/100 (Quiet: %.0f, Wi-Fi: %.0f, Budget: %.0f%s)",
                finalScore, quietScore, wifiScore, budgetScore, isBudgetTight ? " [Boosted]" : "");

        return new ScoredPlace(place, finalScore, breakdown);
    }

    public static List<ScoredPlace> rankPlaces(List<StudyPlace> places, Weights weights, boolean isBudgetTight) {
        List<ScoredPlace> scoredList = new ArrayList<>();
        if (places == null) return scoredList;

        for (StudyPlace place : places) {
            scoredList.add(scorePlace(place, weights, isBudgetTight));
        }

        Collections.sort(scoredList);
        return scoredList;
    }
}

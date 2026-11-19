package com.unimate.dto;

import com.unimate.model.StudyPlace;

public class ScoredPlace implements Comparable<ScoredPlace> {

    private StudyPlace place;
    private double score; // 0.0 to 100.0
    private String breakdown;

    public ScoredPlace(StudyPlace place, double score, String breakdown) {
        this.place = place;
        this.score = score;
        this.breakdown = breakdown;
    }

    public StudyPlace getPlace() {
        return place;
    }

    public double getScore() {
        return score;
    }

    public String getBreakdown() {
        return breakdown;
    }

    @Override
    public int compareTo(ScoredPlace o) {
        // Descending order of score
        return Double.compare(o.score, this.score);
    }
}

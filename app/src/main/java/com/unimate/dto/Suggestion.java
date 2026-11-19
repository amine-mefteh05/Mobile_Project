package com.unimate.dto;

import com.unimate.model.StudyPlace;
import com.unimate.model.Task;

public class Suggestion {

    private String title;
    private String description;
    private String reason;
    private int freeSlotMinutes;
    private Task recommendedTask;
    private StudyPlace recommendedPlace;

    public Suggestion(String title, String description, String reason, int freeSlotMinutes,
                      Task recommendedTask, StudyPlace recommendedPlace) {
        this.title = title;
        this.description = description;
        this.reason = reason;
        this.freeSlotMinutes = freeSlotMinutes;
        this.recommendedTask = recommendedTask;
        this.recommendedPlace = recommendedPlace;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getReason() {
        return reason;
    }

    public int getFreeSlotMinutes() {
        return freeSlotMinutes;
    }

    public Task getRecommendedTask() {
        return recommendedTask;
    }

    public StudyPlace getRecommendedPlace() {
        return recommendedPlace;
    }
}

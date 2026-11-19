package com.unimate.dto;

public class WeeklySummary {

    private int totalTasks;
    private int completedTasks;
    private double completionRate; // percentage
    private double totalStudyHours;
    private double totalSpent;
    private String topCategory;

    public WeeklySummary(int totalTasks, int completedTasks, double completionRate,
                         double totalStudyHours, double totalSpent, String topCategory) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.completionRate = completionRate;
        this.totalStudyHours = totalStudyHours;
        this.totalSpent = totalSpent;
        this.topCategory = topCategory;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public double getTotalStudyHours() {
        return totalStudyHours;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public String getTopCategory() {
        return topCategory;
    }
}

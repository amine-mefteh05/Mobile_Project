package com.unimate.model;

public class Student {

    private String name;
    private String university;
    private String studyLevel;
    private String preferredEnvironment;
    private double weeklyBudget;
    private String dayStart; // HH:mm
    private String dayEnd; // HH:mm
    private boolean notificationsEnabled;

    public Student() {
        this.name = "Alex Dev";
        this.university = "State University";
        this.studyLevel = "Bachelor (Year 3)";
        this.preferredEnvironment = "QUIET";
        this.weeklyBudget = 100.0;
        this.dayStart = "08:00";
        this.dayEnd = "20:00";
        this.notificationsEnabled = true;
    }

    public Student(String name, String university, String studyLevel, String preferredEnvironment,
                   double weeklyBudget, String dayStart, String dayEnd, boolean notificationsEnabled) {
        this.name = name;
        this.university = university;
        this.studyLevel = studyLevel;
        this.preferredEnvironment = preferredEnvironment;
        this.weeklyBudget = weeklyBudget;
        this.dayStart = dayStart;
        this.dayEnd = dayEnd;
        this.notificationsEnabled = notificationsEnabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getStudyLevel() {
        return studyLevel;
    }

    public void setStudyLevel(String studyLevel) {
        this.studyLevel = studyLevel;
    }

    public String getPreferredEnvironment() {
        return preferredEnvironment;
    }

    public void setPreferredEnvironment(String preferredEnvironment) {
        this.preferredEnvironment = preferredEnvironment;
    }

    public double getWeeklyBudget() {
        return weeklyBudget;
    }

    public void setWeeklyBudget(double weeklyBudget) {
        this.weeklyBudget = weeklyBudget;
    }

    public String getDayStart() {
        return dayStart;
    }

    public void setDayStart(String dayStart) {
        this.dayStart = dayStart;
    }

    public String getDayEnd() {
        return dayEnd;
    }

    public void setDayEnd(String dayEnd) {
        this.dayEnd = dayEnd;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}

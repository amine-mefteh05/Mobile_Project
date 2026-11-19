package com.unimate.dto;

import java.time.LocalTime;

public class TimeSlot {

    private String date; // yyyy-MM-dd
    private String startTime; // HH:mm
    private String endTime; // HH:mm

    public TimeSlot(String date, String startTime, String endTime) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public int getDurationMinutes() {
        try {
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);
            int minutes = end.getHour() * 60 + end.getMinute() - (start.getHour() * 60 + start.getMinute());
            return Math.max(0, minutes);
        } catch (Exception e) {
            return 0;
        }
    }
}

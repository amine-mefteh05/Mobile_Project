package com.unimate.model;

import java.time.LocalTime;

public class Event {

    public enum EventType {
        LECTURE,
        PRACTICAL,
        EXAM,
        PERSONAL,
        STUDY
    }

    private long id;
    private int studentId;
    private String title;
    private String date; // yyyy-MM-dd
    private String startTime; // HH:mm
    private String endTime; // HH:mm
    private String location;
    private EventType type;
    private long taskId; // -1 if not linked to a task

    public Event() {
        this.id = -1;
        this.studentId = 1;
        this.type = EventType.PERSONAL;
        this.taskId = -1;
    }

    public Event(long id, int studentId, String title, String date, String startTime,
                 String endTime, String location, EventType type, long taskId) {
        this.id = id;
        this.studentId = studentId;
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.type = type;
        this.taskId = taskId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
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

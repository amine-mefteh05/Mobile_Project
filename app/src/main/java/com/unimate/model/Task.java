package com.unimate.model;

import java.time.LocalDate;
import java.util.Objects;

public class Task {

    public enum Category {
        UNIVERSITY,
        PERSONAL,
        PROJECT,
        EXAM
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    public enum Status {
        PENDING,
        DONE
    }

    private long id;
    private int studentId;
    private String title;
    private String description;
    private Category category;
    private Priority priority;
    private String deadline; // ISO format: yyyy-MM-dd
    private int duration; // in minutes
    private Status status;
    private boolean hasReminder;
    private String reminderTime; // HH:mm format

    public Task() {
        this.id = -1;
        this.studentId = 1;
        this.category = Category.UNIVERSITY;
        this.priority = Priority.MEDIUM;
        this.status = Status.PENDING;
        this.hasReminder = false;
    }

    public Task(long id, int studentId, String title, String description, Category category,
                Priority priority, String deadline, int duration, Status status,
                boolean hasReminder, String reminderTime) {
        this.id = id;
        this.studentId = studentId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.deadline = deadline;
        this.duration = duration;
        this.status = status;
        this.hasReminder = hasReminder;
        this.reminderTime = reminderTime;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isHasReminder() {
        return hasReminder;
    }

    public void setHasReminder(boolean hasReminder) {
        this.hasReminder = hasReminder;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    /**
     * Derived display state for overdue tasks.
     * Not stored directly in database.
     */
    public boolean isOverdue() {
        if (status == Status.DONE || deadline == null || deadline.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDate today = LocalDate.now();
            LocalDate taskDeadline = LocalDate.parse(deadline);
            return taskDeadline.isBefore(today);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

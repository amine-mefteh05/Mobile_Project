package com.unimate.model;

public class Expense {

    public enum Category {
        FOOD,
        TRANSPORT,
        STUDY,
        OTHER
    }

    private long id;
    private int studentId;
    private double amount;
    private Category category;
    private String date; // yyyy-MM-dd
    private String description;

    public Expense() {
        this.id = -1;
        this.studentId = 1;
        this.category = Category.FOOD;
    }

    public Expense(long id, int studentId, double amount, Category category, String date, String description) {
        this.id = id;
        this.studentId = studentId;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

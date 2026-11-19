package com.unimate.model;

public class Opportunity {

    public enum Category {
        WORKSHOP,
        EVENT,
        HACKATHON,
        INTERNSHIP,
        SCHOLARSHIP,
        RESOURCE
    }

    private long id;
    private String title;
    private String description;
    private Category category;
    private String deadline; // yyyy-MM-dd
    private String link;
    private String organization;

    public Opportunity() {
        this.id = -1;
        this.category = Category.WORKSHOP;
    }

    public Opportunity(long id, String title, String description, Category category,
                       String deadline, String link, String organization) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.deadline = deadline;
        this.link = link;
        this.organization = organization;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}

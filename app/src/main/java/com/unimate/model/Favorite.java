package com.unimate.model;

public class Favorite {

    public enum FavoriteType {
        PLACE,
        OPPORTUNITY
    }

    private long id;
    private int studentId;
    private FavoriteType itemType;
    private long itemId;

    public Favorite() {
        this.id = -1;
        this.studentId = 1;
    }

    public Favorite(long id, int studentId, FavoriteType itemType, long itemId) {
        this.id = id;
        this.studentId = studentId;
        this.itemType = itemType;
        this.itemId = itemId;
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

    public FavoriteType getItemType() {
        return itemType;
    }

    public void setItemType(FavoriteType itemType) {
        this.itemType = itemType;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
}

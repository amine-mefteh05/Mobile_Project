package com.unimate.model;

public class StudyPlace {

    private long id;
    private String name;
    private String address;
    private int priceLevel; // 1 ($), 2 ($$), 3 ($$$)
    private int noiseLevel; // 1 (Very Quiet) to 5 (Loud)
    private boolean wifi;
    private boolean outlets;
    private String openingHours;
    private double latitude;
    private double longitude;
    private String category; // e.g. Library, Cafe, Coworking

    public StudyPlace() {
        this.id = -1;
        this.priceLevel = 1;
        this.noiseLevel = 1;
        this.wifi = true;
        this.outlets = true;
    }

    public StudyPlace(long id, String name, String address, int priceLevel, int noiseLevel,
                      boolean wifi, boolean outlets, String openingHours,
                      double latitude, double longitude, String category) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.priceLevel = priceLevel;
        this.noiseLevel = noiseLevel;
        this.wifi = wifi;
        this.outlets = outlets;
        this.openingHours = openingHours;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(int priceLevel) {
        this.priceLevel = priceLevel;
    }

    public int getNoiseLevel() {
        return noiseLevel;
    }

    public void setNoiseLevel(int noiseLevel) {
        this.noiseLevel = noiseLevel;
    }

    public boolean isWifi() {
        return wifi;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public boolean isOutlets() {
        return outlets;
    }

    public void setOutlets(boolean outlets) {
        this.outlets = outlets;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

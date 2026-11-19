package com.unimate.dto;

public class Weights {

    private int quietWeight;
    private int wifiWeight;
    private int budgetWeight;
    private int distanceWeight;

    public Weights() {
        this.quietWeight = 50;
        this.wifiWeight = 50;
        this.budgetWeight = 50;
        this.distanceWeight = 50;
    }

    public Weights(int quietWeight, int wifiWeight, int budgetWeight, int distanceWeight) {
        this.quietWeight = quietWeight;
        this.wifiWeight = wifiWeight;
        this.budgetWeight = budgetWeight;
        this.distanceWeight = distanceWeight;
    }

    public int getQuietWeight() {
        return quietWeight;
    }

    public void setQuietWeight(int quietWeight) {
        this.quietWeight = quietWeight;
    }

    public int getWifiWeight() {
        return wifiWeight;
    }

    public void setWifiWeight(int wifiWeight) {
        this.wifiWeight = wifiWeight;
    }

    public int getBudgetWeight() {
        return budgetWeight;
    }

    public void setBudgetWeight(int budgetWeight) {
        this.budgetWeight = budgetWeight;
    }

    public int getDistanceWeight() {
        return distanceWeight;
    }

    public void setDistanceWeight(int distanceWeight) {
        this.distanceWeight = distanceWeight;
    }

    public int getTotalWeight() {
        return Math.max(1, quietWeight + wifiWeight + budgetWeight + distanceWeight);
    }
}

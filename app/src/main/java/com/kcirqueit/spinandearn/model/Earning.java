package com.kcirqueit.spinandearn.model;

import java.io.Serializable;

public class Earning implements Serializable {

    private String userId;
    private int dailyPoint;
    private int dailySpingCount = 30;
    private int totalPoint;
    private String date;


    public Earning() {
    }

    public Earning(String userId, int dailyPoint, int dailySpingCount, int totalPoint, String date) {
        this.userId = userId;
        this.dailyPoint = dailyPoint;
        this.dailySpingCount = dailySpingCount;
        this.totalPoint = totalPoint;
        this.date = date;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getDailyPoint() {
        return dailyPoint;
    }

    public void setDailyPoint(int dailyPoint) {
        this.dailyPoint = dailyPoint;
    }

    public int getDailySpingCount() {
        return dailySpingCount;
    }

    public void setDailySpingCount(int dailySpingCount) {
        this.dailySpingCount = dailySpingCount;
    }

    public int getTotalPoint() {
        return totalPoint;
    }

    public void setTotalPoint(int totalPoint) {
        this.totalPoint = totalPoint;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

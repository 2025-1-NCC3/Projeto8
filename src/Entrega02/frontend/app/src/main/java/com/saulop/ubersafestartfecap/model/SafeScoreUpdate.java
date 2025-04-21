package com.saulop.ubersafestartfecap.model;

public class SafeScoreUpdate {
    private String userId;
    private int scoreChange;

    public SafeScoreUpdate(String userId, int scoreChange) {
        this.userId = userId;
        this.scoreChange = scoreChange;
    }

    public String getUserId() {
        return userId;
    }

    public int getScoreChange() {
        return scoreChange;
    }
}
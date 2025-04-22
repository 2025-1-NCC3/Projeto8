package com.saulop.ubersafestartfecap.model;

public class SafeScoreResponse {
    private boolean success;
    private String message;
    private int newScore;
    private int safescore;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getNewScore() {
        return newScore > 0 ? newScore : safescore;
    }

    public int getSafescore() {
        return safescore > 0 ? safescore : newScore;
    }
}
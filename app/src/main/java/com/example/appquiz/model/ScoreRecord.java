package com.example.appquiz.model;

public class ScoreRecord {
    private int id;
    private int score;
    private int totalQuestions;
    private String timestamp;

    public ScoreRecord(int id, int score, int totalQuestions, String timestamp) {
        this.id = id;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getPercentage() {
        if (totalQuestions == 0) return 0;
        return ((double) score / totalQuestions) * 100;
    }
}

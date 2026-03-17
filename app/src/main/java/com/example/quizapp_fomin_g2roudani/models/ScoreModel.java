package com.example.quizapp_fomin_g2roudani.models;

public class ScoreModel {
    private int score;
    private int total;
    public ScoreModel(int score, int total) {
        this.score = score;
        this.total = total;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}


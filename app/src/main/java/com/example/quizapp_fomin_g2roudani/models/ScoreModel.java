package com.example.quizapp_fomin_g2roudani.models;

public class ScoreModel {

    private int score;
    private int total;

    // ✅ Constructeur vide requis par Gson
    public ScoreModel() {}

    public int getScore() {
        return score;
    }

    public int getTotal() {
        return total;
    }
}
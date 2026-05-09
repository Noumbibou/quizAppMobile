package com.example.quizapp_fomin_g2roudani.models;

import com.google.gson.annotations.SerializedName;

public class LeaderboardEntry {
    @SerializedName("score")
    private int score;

    @SerializedName("total")
    private int total;

    // Getters
    public int getScore() { return score; }
    public int getTotal() { return total; }
}

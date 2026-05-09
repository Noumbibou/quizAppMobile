package com.example.quizapp_fomin_g2roudani.models;

import com.google.gson.annotations.SerializedName;

public class MyStatsSummary {
    @SerializedName("games_played")
    private int gamesPlayed;

    @SerializedName("average_score")
    private float averageScore;

    @SerializedName("best_score")
    private int bestScore;

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public float getAverageScore() {
        return averageScore;
    }

    public int getBestScore() {
        return bestScore;
    }
}

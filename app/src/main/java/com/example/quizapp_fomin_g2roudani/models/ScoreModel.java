package com.example.quizapp_fomin_g2roudani.models;

import com.google.gson.annotations.SerializedName;

public class ScoreModel {
    @SerializedName("score")
    private int score;

    @SerializedName("total")
    private int total;

    @SerializedName("cheated_flag")
    private boolean cheatedFlag; // Retourné par le backend si la triche est validée

    public int getScore() { return score; }
    public int getTotal() { return total; }
    public boolean isCheatedFlag() { return cheatedFlag; }
}

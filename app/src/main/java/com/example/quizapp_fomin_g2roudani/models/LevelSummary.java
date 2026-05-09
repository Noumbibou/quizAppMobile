package com.example.quizapp_fomin_g2roudani.models;

import com.google.gson.annotations.SerializedName;

public class LevelSummary {
    private String level;

    @SerializedName("avg_score")
    private float avgScore;
    
    @SerializedName("max_score")
    private int maxScore;
    
    @SerializedName("total_played")
    private int totalPlayed;

    public String getLevel() { return level; }
    public float getAvgScore() { return avgScore; }
    public int getMaxScore() { return maxScore; }
    public int getTotalPlayed() { return totalPlayed; }
}

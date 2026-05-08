package com.example.quizapp_fomin_g2roudani.models;

import com.google.gson.annotations.SerializedName;

public class UserScore {
    private int score;
    private int total;
    private String level;
    
    @SerializedName("created_at")
    private String createdAt;

    public int getScore() { return score; }
    public int getTotal() { return total; }
    public String getLevel() { return level; }
    public String getCreatedAt() { return createdAt; }
}

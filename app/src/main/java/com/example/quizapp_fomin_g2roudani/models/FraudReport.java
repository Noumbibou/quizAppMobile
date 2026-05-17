package com.example.quizapp_fomin_g2roudani.models;

import com.google.gson.annotations.SerializedName;

public class FraudReport {
    @SerializedName("id") private int id;
    @SerializedName("email") private String userEmail;
    @SerializedName("level") private String level;
    @SerializedName("score") private int score;
    @SerializedName("total") private int total;
    @SerializedName("cheat_score") private int cheatScore;
    @SerializedName("time_spent") private int timeSpent;
    @SerializedName("latitude") private Double latitude;
    @SerializedName("longitude") private Double longitude;
    @SerializedName("camera_active") private boolean cameraActive;
    @SerializedName("created_at") private String createdAt;

    public String getUserEmail() { return userEmail; }
    public String getLevel() { return level; }
    public int getScore() { return score; }
    public int getTotal() { return total; }
    public int getCheatScore() { return cheatScore; }
    public int getTimeSpent() { return timeSpent; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public boolean isCameraActive() { return cameraActive; }
    public String getCreatedAt() { return createdAt; }
}

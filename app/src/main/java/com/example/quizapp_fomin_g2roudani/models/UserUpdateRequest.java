package com.example.quizapp_fomin_g2roudani.models;

import com.google.gson.annotations.SerializedName;

public class UserUpdateRequest {
    @SerializedName("username")
    private String username;

    public UserUpdateRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}

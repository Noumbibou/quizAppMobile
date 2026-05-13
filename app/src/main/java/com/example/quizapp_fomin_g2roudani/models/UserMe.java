package com.example.quizapp_fomin_g2roudani.models;

import com.google.gson.annotations.SerializedName;

public class UserMe {
    @SerializedName("uid")
    private String uid;

    @SerializedName("email")
    private String email;

    @SerializedName("admin")
    private boolean admin;

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAdmin() {
        return admin;
    }
}

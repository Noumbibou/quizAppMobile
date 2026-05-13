package com.example.quizapp_fomin_g2roudani.models;

import com.google.gson.annotations.SerializedName;

public class AdminUser {
    @SerializedName("uid")
    private String uid;

    @SerializedName("email")
    private String email;

    @SerializedName("is_admin")
    private boolean admin;

    @SerializedName("is_disabled")
    private boolean disabled;

    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public boolean isAdmin() { return admin; }
    public boolean isDisabled() { return disabled; }
}

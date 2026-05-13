package com.example.quizapp_fomin_g2roudani.network;

import com.example.quizapp_fomin_g2roudani.models.UserMe;
import retrofit2.Call;
import retrofit2.http.GET;

public interface AuthApi {

    @GET("me")
    Call<UserMe> getMe();
}

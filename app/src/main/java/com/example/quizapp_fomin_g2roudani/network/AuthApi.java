package com.example.quizapp_fomin_g2roudani.network;

import com.example.quizapp_fomin_g2roudani.models.UserMe;
import com.example.quizapp_fomin_g2roudani.models.UserUpdateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface AuthApi {

    @GET("me")
    Call<UserMe> getMe();

    @PUT("/user/update-profile")
    Call<Void> updateUserProfile(@Body UserUpdateRequest request);
}

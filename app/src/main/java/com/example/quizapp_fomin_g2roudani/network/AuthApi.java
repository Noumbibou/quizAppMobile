package com.example.quizapp_fomin_g2roudani.network;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.GET;

public interface AuthApi {

    @GET("me")
    Call<Map<String, Object>> getMe();
}

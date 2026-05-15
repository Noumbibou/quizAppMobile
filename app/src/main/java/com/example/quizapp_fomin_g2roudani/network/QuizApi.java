package com.example.quizapp_fomin_g2roudani.network;

import com.example.quizapp_fomin_g2roudani.models.QuizResponse;
import com.example.quizapp_fomin_g2roudani.models.ScoreModel;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface QuizApi {

    @GET("quiz/questions")
    Call<QuizResponse> getQuestions(
            @Query("level") String level
    );

    @POST("quiz/score")
    Call<ScoreModel> submitScore(@Body Map<String, Object> payload);

}
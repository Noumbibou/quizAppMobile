package com.example.quizapp_fomin_g2roudani.network;

import com.example.quizapp_fomin_g2roudani.models.LeaderboardEntry;
import com.example.quizapp_fomin_g2roudani.models.MyStatsSummary;
import com.example.quizapp_fomin_g2roudani.models.UserScore;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StatsApi {
    @GET("stats/leaderboard")
    Call<List<LeaderboardEntry>> getLeaderboard(@Query("level") String level);

    @GET("stats/my-scores")
    Call<List<UserScore>> getMyScores();

    @GET("stats/my-summary")
    Call<MyStatsSummary> getMySummary();
}

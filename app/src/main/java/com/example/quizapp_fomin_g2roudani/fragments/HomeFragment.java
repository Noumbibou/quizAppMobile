package com.example.quizapp_fomin_g2roudani.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quizapp_fomin_g2roudani.MapActivity;
import com.example.quizapp_fomin_g2roudani.R;
import com.example.quizapp_fomin_g2roudani.SelectLevel;
import com.example.quizapp_fomin_g2roudani.models.MyStatsSummary;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.example.quizapp_fomin_g2roudani.network.StatsApi;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextView tvUserNameHome, tvTotalPlayed, tvAvgScore, tvMaxScore;
    private LinearProgressIndicator loader;
    private StatsApi statsApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvUserNameHome = view.findViewById(R.id.tvUserNameHome);
        loader = view.findViewById(R.id.loaderHome);

        View cardPlayed = view.findViewById(R.id.cardPlayedHome);
        View cardAvg = view.findViewById(R.id.cardAvgHome);
        View cardBest = view.findViewById(R.id.cardBestHome);

        if (cardPlayed != null) {
            tvTotalPlayed = cardPlayed.findViewById(R.id.tvStatValue);
            TextView label = cardPlayed.findViewById(R.id.tvStatLabel);
            if (label != null) label.setText(R.string.stat_played);
        }

        if (cardAvg != null) {
            tvAvgScore = cardAvg.findViewById(R.id.tvStatValue);
            TextView label = cardAvg.findViewById(R.id.tvStatLabel);
            if (label != null) label.setText(R.string.stat_avg);
        }

        if (cardBest != null) {
            tvMaxScore = cardBest.findViewById(R.id.tvStatValue);
            TextView label = cardBest.findViewById(R.id.tvStatLabel);
            if (label != null) label.setText(R.string.stat_best);
        }

        view.findViewById(R.id.btnStartQuizHome).setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), SelectLevel.class)));

        // ✅ Navigation vers MapActivity
        view.findViewById(R.id.btnOpenMapHome).setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), MapActivity.class)));

        statsApi = ApiClient.getClient().create(StatsApi.class);

        setupUserInfo();
        fetchStats();

        return view;
    }

    private void setupUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = user.getEmail() != null ? user.getEmail().split("@")[0] : getString(R.string.nav_profile);
            }
            tvUserNameHome.setText(name);
        }
    }

    private void fetchStats() {
        if (loader != null) loader.setVisibility(View.VISIBLE);
        statsApi.getMySummary().enqueue(new Callback<MyStatsSummary>() {
            @Override
            public void onResponse(Call<MyStatsSummary> call, Response<MyStatsSummary> response) {
                if (isAdded() && loader != null) loader.setVisibility(View.GONE);
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    updateStatsUI(response.body());
                }
            }

            @Override
            public void onFailure(Call<MyStatsSummary> call, Throwable t) {
                if (isAdded() && loader != null) loader.setVisibility(View.GONE);
                Log.e("HomeFragment", "Error: " + t.getMessage());
            }
        });
    }

    private void updateStatsUI(MyStatsSummary summary) {
        if (tvTotalPlayed != null) tvTotalPlayed.setText(String.valueOf(summary.getGamesPlayed()));
        if (tvMaxScore != null) tvMaxScore.setText(String.valueOf(summary.getBestScore()));
        if (tvAvgScore != null) tvAvgScore.setText(String.format(Locale.FRANCE, "%.1f%%", summary.getAverageScore()));
    }
}

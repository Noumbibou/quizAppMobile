package com.example.quizapp_fomin_g2roudani.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp_fomin_g2roudani.R;
import com.example.quizapp_fomin_g2roudani.SelectLevel;
import com.example.quizapp_fomin_g2roudani.adapters.LeaderboardAdapter;
import com.example.quizapp_fomin_g2roudani.models.LeaderboardEntry;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.example.quizapp_fomin_g2roudani.network.StatsApi;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardFragment extends Fragment {
    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private List<LeaderboardEntry> leaderboardList = new ArrayList<>();
    private StatsApi statsApi;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        
        tabLayout = view.findViewById(R.id.tabLayoutLevels);
        recyclerView = view.findViewById(R.id.rvLeaderboard);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaderboardAdapter(leaderboardList);
        recyclerView.setAdapter(adapter);

        statsApi = ApiClient.getClient().create(StatsApi.class);

        setupTabs();
        loadData(SelectLevel.LEVEL_BEGINNER);

        return view;
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String level;
                switch (tab.getPosition()) {
                    case 1: level = SelectLevel.LEVEL_INTERMEDIATE; break;
                    case 2: level = SelectLevel.LEVEL_ADVANCED; break;
                    default: level = SelectLevel.LEVEL_BEGINNER; break;
                }
                loadData(level);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadData(String level) {
        statsApi.getLeaderboard(level).enqueue(new Callback<List<LeaderboardEntry>>() {
            @Override
            public void onResponse(Call<List<LeaderboardEntry>> call, Response<List<LeaderboardEntry>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    leaderboardList.clear();
                    leaderboardList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<LeaderboardEntry>> call, Throwable t) {}
        });
    }
}

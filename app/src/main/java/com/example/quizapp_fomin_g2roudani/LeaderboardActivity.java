package com.example.quizapp_fomin_g2roudani;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.quizapp_fomin_g2roudani.adapters.LeaderboardAdapter;
import com.example.quizapp_fomin_g2roudani.models.LeaderboardEntry;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.example.quizapp_fomin_g2roudani.network.StatsApi;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvLeaderboard;
    private TabLayout tabLayoutLevels;
    private SwipeRefreshLayout swipeRefresh;
    private LinearProgressIndicator progressIndicator;
    private StatsApi statsApi;
    private LeaderboardAdapter adapter;
    private final List<LeaderboardEntry> leaderboardList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        initViews();
        setupNetwork();
        setupRecyclerView();
        setupListeners();

        // Charger le premier niveau par défaut
        loadLeaderboard(SelectLevel.LEVEL_BEGINNER);
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        tabLayoutLevels = findViewById(R.id.tabLayoutLevels);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressIndicator = findViewById(R.id.progressIndicator);
    }

    private void setupNetwork() {
        statsApi = ApiClient.getClient().create(StatsApi.class);
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter(leaderboardList);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        rvLeaderboard.setAdapter(adapter);
    }

    private void setupListeners() {
        tabLayoutLevels.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String level = getLevelFromTab(tab.getPosition());
                loadLeaderboard(level);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        swipeRefresh.setOnRefreshListener(() -> {
            String level = getLevelFromTab(tabLayoutLevels.getSelectedTabPosition());
            loadLeaderboard(level);
        });
    }

    private String getLevelFromTab(int position) {
        switch (position) {
            case 1: return SelectLevel.LEVEL_INTERMEDIATE;
            case 2: return SelectLevel.LEVEL_ADVANCED;
            default: return SelectLevel.LEVEL_BEGINNER;
        }
    }

    private void loadLeaderboard(String level) {
        setLoading(true);
        statsApi.getLeaderboard(level).enqueue(new Callback<List<LeaderboardEntry>>() {
            @Override
            public void onResponse(Call<List<LeaderboardEntry>> call, Response<List<LeaderboardEntry>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    leaderboardList.clear();
                    leaderboardList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(LeaderboardActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<LeaderboardEntry>> call, Throwable t) {
                setLoading(false);
                Log.e("Leaderboard", "Error: " + t.getMessage());
                Toast.makeText(LeaderboardActivity.this, "Impossible de contacter le serveur", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        swipeRefresh.setRefreshing(isLoading);
    }
}

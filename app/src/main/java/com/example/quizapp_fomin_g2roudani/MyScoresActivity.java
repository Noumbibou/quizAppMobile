package com.example.quizapp_fomin_g2roudani;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.quizapp_fomin_g2roudani.adapters.MyScoresAdapter;
import com.example.quizapp_fomin_g2roudani.models.UserScore;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.example.quizapp_fomin_g2roudani.network.StatsApi;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyScoresActivity extends AppCompatActivity {

    private RecyclerView rvMyScores;
    private SwipeRefreshLayout swipeRefresh;
    private LinearProgressIndicator progressIndicator;
    private LinearLayout emptyState;
    private StatsApi statsApi;
    private MyScoresAdapter adapter;
    private final List<UserScore> scoreList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_scores);

        initViews();
        setupNetwork();
        setupRecyclerView();
        setupListeners();

        loadMyScores();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvMyScores = findViewById(R.id.rvMyScores);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressIndicator = findViewById(R.id.progressIndicator);
        emptyState = findViewById(R.id.emptyState);
    }

    private void setupNetwork() {
        statsApi = ApiClient.getClient().create(StatsApi.class);
    }

    private void setupRecyclerView() {
        adapter = new MyScoresAdapter(scoreList);
        rvMyScores.setLayoutManager(new LinearLayoutManager(this));
        rvMyScores.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadMyScores);
    }

    private void loadMyScores() {
        setLoading(true);
        statsApi.getMyScores().enqueue(new Callback<List<UserScore>>() {
            @Override
            public void onResponse(Call<List<UserScore>> call, Response<List<UserScore>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    scoreList.clear();
                    scoreList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    
                    updateEmptyState();
                } else {
                    Toast.makeText(MyScoresActivity.this, "Erreur lors de la récupération des scores", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserScore>> call, Throwable t) {
                setLoading(false);
                Log.e("MyScores", "Error: " + t.getMessage());
                Toast.makeText(MyScoresActivity.this, "Impossible de contacter le serveur", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (scoreList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvMyScores.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvMyScores.setVisibility(View.VISIBLE);
        }
    }

    private void setLoading(boolean isLoading) {
        progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        swipeRefresh.setRefreshing(isLoading);
    }
}

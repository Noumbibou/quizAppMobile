package com.example.quizapp_fomin_g2roudani.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.quizapp_fomin_g2roudani.R;
import com.example.quizapp_fomin_g2roudani.adapters.MyScoresAdapter;
import com.example.quizapp_fomin_g2roudani.models.UserScore;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.example.quizapp_fomin_g2roudani.network.StatsApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private MyScoresAdapter adapter;
    private final List<UserScore> scoreList = new ArrayList<>();
    private StatsApi statsApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.rvHistory);
        swipeRefresh = view.findViewById(R.id.swipeRefreshHistory);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyScoresAdapter(scoreList);
        recyclerView.setAdapter(adapter);

        statsApi = ApiClient.getClient().create(StatsApi.class);
        
        swipeRefresh.setOnRefreshListener(this::loadHistory);
        
        loadHistory();

        return view;
    }

    private void loadHistory() {
        swipeRefresh.setRefreshing(true);
        statsApi.getMyScores().enqueue(new Callback<List<UserScore>>() {
            @Override
            public void onResponse(Call<List<UserScore>> call, Response<List<UserScore>> response) {
                swipeRefresh.setRefreshing(false);
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    scoreList.clear();
                    scoreList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else if (isAdded()) {
                    Toast.makeText(getContext(), "Erreur lors de la récupération de l'historique", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserScore>> call, Throwable t) {
                if (isAdded()) {
                    swipeRefresh.setRefreshing(false);
                    Log.e("HistoryFragment", "Error: " + t.getMessage());
                    Toast.makeText(getContext(), "Impossible de contacter le serveur", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

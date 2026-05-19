package com.example.quizapp_fomin_g2roudani.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.quizapp_fomin_g2roudani.R;
import com.example.quizapp_fomin_g2roudani.models.UserScore;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.example.quizapp_fomin_g2roudani.network.StatsApi;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatsFragment extends Fragment {

    private LineChart chart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        chart = view.findViewById(R.id.lineChart);
        
        fetchStatsData();
        
        return view;
    }

    private void fetchStatsData() {
        StatsApi statsApi = ApiClient.getClient().create(StatsApi.class);
        statsApi.getMyScores().enqueue(new Callback<List<UserScore>>() {
            @Override
            public void onResponse(Call<List<UserScore>> call, Response<List<UserScore>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    setupChart(chart, response.body());
                } else if (isAdded()) {
                    Log.e("StatsFragment", "Erreur API lors de la récupération des scores");
                }
            }

            @Override
            public void onFailure(Call<List<UserScore>> call, Throwable t) {
                if (isAdded()) {
                    Log.e("StatsFragment", "Échec de connexion : " + t.getMessage());
                }
            }
        });
    }

    private void setupChart(LineChart chart, List<UserScore> scores) {
        List<Entry> entries = new ArrayList<>();
        
        // Conversion des scores réels en entrées de graphique
        for (int i = 0; i < scores.size(); i++) {
            entries.add(new Entry(i, scores.get(i).getScore()));
        }

        if (entries.isEmpty()) {
            chart.setNoDataText("Aucune donnée disponible");
            chart.invalidate();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Progression réelle");
        dataSet.setColor(getResources().getColor(R.color.primary));
        dataSet.setLineWidth(3f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.primary));
        dataSet.setFillAlpha(50);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(getResources().getColor(R.color.primary));

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.animateY(1000);
        chart.invalidate();
    }
}

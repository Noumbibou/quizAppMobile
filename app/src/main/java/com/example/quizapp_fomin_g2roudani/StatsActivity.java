package com.example.quizapp_fomin_g2roudani;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp_fomin_g2roudani.models.MyStatsSummary;
import com.example.quizapp_fomin_g2roudani.models.UserScore;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.example.quizapp_fomin_g2roudani.network.StatsApi;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatsActivity extends AppCompatActivity {

    private LineChart lineChart;
    private BarChart barChart;
    private TextView tvTotalGames, tvAvgGlobal;
    private LinearProgressIndicator progressIndicator;
    private StatsApi statsApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        initViews();
        setupNetwork();
        
        fetchAllStats();
    }

    private void initViews() {
        lineChart = findViewById(R.id.lineChart);
        barChart = findViewById(R.id.barChart);
        tvTotalGames = findViewById(R.id.tvTotalGames);
        tvAvgGlobal = findViewById(R.id.tvAvgGlobal);
        progressIndicator = findViewById(R.id.progressIndicator);
        
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        setupChartStyle(lineChart);
        setupChartStyle(barChart);
    }

    private void setupNetwork() {
        statsApi = ApiClient.getClient().create(StatsApi.class);
    }

    private void setupChartStyle(Chart<?> chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setForm(Legend.LegendForm.CIRCLE);
        chart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        chart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        chart.getLegend().setDrawInside(false);
        
        chart.animateY(1000);
        chart.setNoDataText("Chargement des données...");
        
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
    }

    private void fetchAllStats() {
        setLoading(true);
        fetchLineChartData();
        fetchSummaryStats();
    }

    private void fetchLineChartData() {
        statsApi.getMyScores().enqueue(new Callback<List<UserScore>>() {
            @Override
            public void onResponse(Call<List<UserScore>> call, Response<List<UserScore>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayLineChart(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<UserScore>> call, Throwable t) {
                Log.e("StatsActivity", "LineChart error: " + t.getMessage());
            }
        });
    }

    private void displayLineChart(List<UserScore> scores) {
        if (scores.isEmpty()) {
            lineChart.setNoDataText("Aucun score enregistré");
            lineChart.invalidate();
            return;
        }

        List<UserScore> sortedScores = new ArrayList<>(scores);
        Collections.reverse(sortedScores);

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < sortedScores.size(); i++) {
            entries.add(new Entry(i, sortedScores.get(i).getScore()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Scores");
        dataSet.setColor(Color.parseColor("#2962FF"));
        dataSet.setCircleColor(Color.parseColor("#2962FF"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#D7E2FF"));
        dataSet.setDrawValues(false);

        lineChart.setData(new LineData(dataSet));
        lineChart.invalidate();
    }

    private void fetchSummaryStats() {
        // On utilise l'endpoint global recommandé par vos instructions
        statsApi.getMySummary().enqueue(new Callback<MyStatsSummary>() {
            @Override
            public void onResponse(Call<MyStatsSummary> call, Response<MyStatsSummary> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    updateQuickStats(response.body());
                    // Le BarChart par niveau nécessite des données par niveau (endpoint summary?level=...)
                    barChart.setNoDataText("Détails par niveau bientôt disponibles");
                    barChart.invalidate();
                }
            }

            @Override
            public void onFailure(Call<MyStatsSummary> call, Throwable t) {
                setLoading(false);
                Log.e("StatsActivity", "Summary error: " + t.getMessage());
            }
        });
    }

    private void updateQuickStats(MyStatsSummary summary) {
        tvTotalGames.setText(String.valueOf(summary.getGamesPlayed()));
        tvAvgGlobal.setText(String.format(Locale.FRANCE, "%.1f%%", summary.getAverageScore()));
    }

    private void setLoading(boolean isLoading) {
        progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}

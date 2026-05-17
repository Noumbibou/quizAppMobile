package com.example.quizapp_fomin_g2roudani;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.quizapp_fomin_g2roudani.adapters.FraudReportAdapter;
import com.example.quizapp_fomin_g2roudani.auth.AuthTokenManager;
import com.example.quizapp_fomin_g2roudani.models.FraudReport;
import com.example.quizapp_fomin_g2roudani.network.AdminApi;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FraudReportsActivity extends AppCompatActivity {

    private static final String TAG = "FraudReports";
    private RecyclerView rvFrauds;
    private SwipeRefreshLayout swipeRefresh;
    private LinearProgressIndicator progressIndicator;
    private LinearLayout emptyState;
    private FraudReportAdapter adapter;
    private final List<FraudReport> fraudList = new ArrayList<>();
    private AdminApi adminApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthTokenManager.isAdmin()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_fraud_reports);

        initViews();
        setupNetwork();
        setupRecyclerView();
        setupListeners();

        loadFrauds();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarFrauds);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvFrauds = findViewById(R.id.rvFrauds);
        swipeRefresh = findViewById(R.id.swipeRefreshFrauds);
        progressIndicator = findViewById(R.id.progressFrauds);
        emptyState = findViewById(R.id.emptyStateFrauds);
    }

    private void setupNetwork() {
        adminApi = ApiClient.getClient().create(AdminApi.class);
    }

    private void setupRecyclerView() {
        adapter = new FraudReportAdapter(fraudList);
        rvFrauds.setLayoutManager(new LinearLayoutManager(this));
        rvFrauds.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadFrauds);
    }

    private void loadFrauds() {
        setLoading(true);
        adminApi.getFraudReports().enqueue(new Callback<List<FraudReport>>() {
            @Override
            public void onResponse(@NonNull Call<List<FraudReport>> call, @NonNull Response<List<FraudReport>> response) {
                if (isFinishing()) return;
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    fraudList.clear();
                    fraudList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                } else {
                    Toast.makeText(FraudReportsActivity.this, "Erreur " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FraudReport>> call, @NonNull Throwable t) {
                if (isFinishing()) return;
                setLoading(false);
                Log.e(TAG, "Load frauds failed", t);
                Toast.makeText(FraudReportsActivity.this, "Serveur inaccessible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (fraudList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvFrauds.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvFrauds.setVisibility(View.VISIBLE);
        }
    }

    private void setLoading(boolean isLoading) {
        progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        swipeRefresh.setRefreshing(isLoading);
    }
}

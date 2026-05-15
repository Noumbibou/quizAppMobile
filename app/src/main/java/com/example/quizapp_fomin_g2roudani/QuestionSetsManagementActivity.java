package com.example.quizapp_fomin_g2roudani;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.quizapp_fomin_g2roudani.adapters.QuestionSetAdapter;
import com.example.quizapp_fomin_g2roudani.auth.AuthTokenManager;
import com.example.quizapp_fomin_g2roudani.models.QuestionSet;
import com.example.quizapp_fomin_g2roudani.network.AdminApi;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionSetsManagementActivity extends AppCompatActivity implements QuestionSetAdapter.OnSetClickListener {

    private static final String TAG = "SetManagement";
    private RecyclerView rvSets;
    private SwipeRefreshLayout swipeRefresh;
    private LinearProgressIndicator progressIndicator;
    private FloatingActionButton fabAdd;
    private QuestionSetAdapter adapter;
    private final List<QuestionSet> setList = new ArrayList<>();
    private AdminApi adminApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthTokenManager.isAdmin()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_question_sets_management);

        initViews();
        setupNetwork();
        setupRecyclerView();
        setupListeners();

        loadSets();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarSets);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvSets = findViewById(R.id.rvQuestionSets);
        swipeRefresh = findViewById(R.id.swipeRefreshSets);
        progressIndicator = findViewById(R.id.progressSets);
        fabAdd = findViewById(R.id.fabAddSet);
    }

    private void setupNetwork() {
        adminApi = ApiClient.getClient().create(AdminApi.class);
    }

    private void setupRecyclerView() {
        adapter = new QuestionSetAdapter(setList, this);
        rvSets.setLayoutManager(new LinearLayoutManager(this));
        rvSets.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadSets);
        fabAdd.setOnClickListener(v -> showAddSetDialog());
    }

    private void loadSets() {
        setLoading(true);
        adminApi.getAllQuestionSets().enqueue(new Callback<List<QuestionSet>>() {
            @Override
            public void onResponse(@NonNull Call<List<QuestionSet>> call, @NonNull Response<List<QuestionSet>> response) {
                if (isFinishing()) return;
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    setList.clear();
                    setList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(QuestionSetsManagementActivity.this, "Erreur " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<QuestionSet>> call, @NonNull Throwable t) {
                if (isFinishing()) return;
                setLoading(false);
                Toast.makeText(QuestionSetsManagementActivity.this, "Serveur inaccessible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddSetDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_set_form, null);
        TextInputEditText etName = view.findViewById(R.id.etSetName);
        AutoCompleteTextView spinnerLevel = view.findViewById(R.id.spinnerSetLevel);

        String[] levels = {"beginner", "intermediate", "advanced"};
        spinnerLevel.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, levels));

        new MaterialAlertDialogBuilder(this)
                .setTitle("Nouveau Pack")
                .setView(view)
                .setPositiveButton("Créer", (dialog, which) -> {
                    String name = etName.getText().toString();
                    String level = spinnerLevel.getText().toString();
                    if (!name.isEmpty() && !level.isEmpty()) {
                        createSet(name, level);
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void createSet(String name, String level) {
        QuestionSet newSet = new QuestionSet();
        newSet.setName(name);
        newSet.setLevel(level);

        setLoading(true);
        adminApi.createQuestionSet(newSet).enqueue(new Callback<QuestionSet>() {
            @Override
            public void onResponse(@NonNull Call<QuestionSet> call, @NonNull Response<QuestionSet> response) {
                if (response.isSuccessful()) {
                    loadSets();
                } else {
                    setLoading(false);
                    Toast.makeText(QuestionSetsManagementActivity.this, "Échec création", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<QuestionSet> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(QuestionSetsManagementActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSetClick(QuestionSet set) {
        String[] options = {"Voir les questions", set.isActive() ? "Déjà actif" : "Activer ce pack"};
        new MaterialAlertDialogBuilder(this)
                .setTitle(set.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(this, QuestionsBySetActivity.class);
                        intent.putExtra("set_id", set.getId());
                        intent.putExtra("set_name", set.getName());
                        intent.putExtra("set_level", set.getLevel());
                        startActivity(intent);
                    } else if (!set.isActive()) {
                        activateSet(set.getId());
                    }
                })
                .show();
    }

    private void activateSet(int id) {
        setLoading(true);
        adminApi.activateQuestionSet(id).enqueue(new Callback<QuestionSet>() {
            @Override
            public void onResponse(@NonNull Call<QuestionSet> call, @NonNull Response<QuestionSet> response) {
                loadSets();
            }

            @Override
            public void onFailure(@NonNull Call<QuestionSet> call, @NonNull Throwable t) {
                setLoading(false);
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        swipeRefresh.setRefreshing(isLoading);
    }
}

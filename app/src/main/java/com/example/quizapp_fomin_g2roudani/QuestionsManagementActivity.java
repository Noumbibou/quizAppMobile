package com.example.quizapp_fomin_g2roudani;

import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.quizapp_fomin_g2roudani.adapters.QuestionAdapter;
import com.example.quizapp_fomin_g2roudani.auth.AuthTokenManager;
import com.example.quizapp_fomin_g2roudani.models.AdminQuestion;
import com.example.quizapp_fomin_g2roudani.models.QuestionCreateRequest;
import com.example.quizapp_fomin_g2roudani.models.QuestionUpdateRequest;
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

public class QuestionsManagementActivity extends AppCompatActivity implements QuestionAdapter.OnQuestionClickListener {

    private static final String TAG = "QuestionsManagement";
    private RecyclerView rvQuestions;
    private SwipeRefreshLayout swipeRefresh;
    private LinearProgressIndicator progressIndicator;
    private FloatingActionButton fabAdd;
    private QuestionAdapter adapter;
    private final List<AdminQuestion> questionList = new ArrayList<>();
    private AdminApi adminApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthTokenManager.isAdmin()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_questions_management);

        initViews();
        setupNetwork();
        setupRecyclerView();
        setupListeners();

        loadQuestions();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarQuestions);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvQuestions = findViewById(R.id.rvQuestions);
        swipeRefresh = findViewById(R.id.swipeRefreshQuestions);
        progressIndicator = findViewById(R.id.progressQuestions);
        fabAdd = findViewById(R.id.fabAddQuestion);
    }

    private void setupNetwork() {
        adminApi = ApiClient.getClient().create(AdminApi.class);
    }

    private void setupRecyclerView() {
        adapter = new QuestionAdapter(questionList, this);
        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvQuestions.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadQuestions);
        fabAdd.setOnClickListener(v -> showQuestionDialog(null));
    }

    private void loadQuestions() {
        setLoading(true);
        adminApi.getAllQuestions().enqueue(new Callback<List<AdminQuestion>>() {
            @Override
            public void onResponse(@NonNull Call<List<AdminQuestion>> call, @NonNull Response<List<AdminQuestion>> response) {
                if (isFinishing()) return;
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    questionList.clear();
                    questionList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(QuestionsManagementActivity.this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AdminQuestion>> call, @NonNull Throwable t) {
                if (isFinishing()) return;
                setLoading(false);
                Log.e(TAG, "Load questions failed", t);
                Toast.makeText(QuestionsManagementActivity.this, "Serveur inaccessible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onQuestionClick(AdminQuestion question) {
        String[] options = {"Modifier", question.isActive() ? "Désactiver" : "Réactiver"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Actions sur la question")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showQuestionDialog(question);
                    } else {
                        handleDeactivate(question);
                    }
                })
                .show();
    }

    private void showQuestionDialog(AdminQuestion questionToEdit) {
        View view = getLayoutInflater().inflate(R.layout.dialog_question_form, null);
        
        TextInputEditText etQuestion = view.findViewById(R.id.etQuestionText);
        TextInputEditText etA = view.findViewById(R.id.etOptionA);
        TextInputEditText etB = view.findViewById(R.id.etOptionB);
        TextInputEditText etC = view.findViewById(R.id.etOptionC);
        TextInputEditText etD = view.findViewById(R.id.etOptionD);
        AutoCompleteTextView spinnerCorrect = view.findViewById(R.id.spinnerCorrectOption);
        AutoCompleteTextView spinnerLevel = view.findViewById(R.id.spinnerLevel);

        // Setup Spinners
        String[] correctOptions = {"A", "B", "C", "D"};
        spinnerCorrect.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, correctOptions));
        
        String[] levels = {"beginner", "intermediate", "advanced"};
        spinnerLevel.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, levels));

        if (questionToEdit != null) {
            etQuestion.setText(questionToEdit.getQuestion());
            etA.setText(questionToEdit.getOptionA());
            etB.setText(questionToEdit.getOptionB());
            etC.setText(questionToEdit.getOptionC());
            etD.setText(questionToEdit.getOptionD());
            spinnerCorrect.setText(questionToEdit.getCorrectOption(), false);
            spinnerLevel.setText(questionToEdit.getLevel(), false);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(questionToEdit == null ? "Ajouter une question" : "Modifier la question")
                .setView(view)
                .setPositiveButton("Enregistrer", (dialog, which) -> {
                    AdminQuestion q = (questionToEdit == null) ? new AdminQuestion() : questionToEdit;
                    q.setQuestion(etQuestion.getText().toString());
                    q.setOptionA(etA.getText().toString());
                    q.setOptionB(etB.getText().toString());
                    q.setOptionC(etC.getText().toString());
                    q.setOptionD(etD.getText().toString());
                    q.setCorrectOption(spinnerCorrect.getText().toString());
                    q.setLevel(spinnerLevel.getText().toString());
                    
                    if (validateForm(q)) {
                        saveQuestion(q, questionToEdit == null);
                    } else {
                        Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private boolean validateForm(AdminQuestion q) {
        return !TextUtils.isEmpty(q.getQuestion()) &&
               !TextUtils.isEmpty(q.getOptionA()) &&
               !TextUtils.isEmpty(q.getOptionB()) &&
               !TextUtils.isEmpty(q.getOptionC()) &&
               !TextUtils.isEmpty(q.getOptionD()) &&
               !TextUtils.isEmpty(q.getCorrectOption()) &&
               !TextUtils.isEmpty(q.getLevel());
    }

    private void saveQuestion(AdminQuestion q, boolean isNew) {
        setLoading(true);
        Call<AdminQuestion> call;
        
        if (isNew) {
            QuestionCreateRequest request = new QuestionCreateRequest(
                q.getQuestion(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD(),
                q.getCorrectOption(),
                q.getLevel()
            );
            call = adminApi.createQuestion(request);
        } else {
            QuestionUpdateRequest request = new QuestionUpdateRequest(
                q.getQuestion(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD(),
                q.getCorrectOption(),
                q.getLevel()
            );
            call = adminApi.updateQuestion(q.getId(), request);
        }

        call.enqueue(new Callback<AdminQuestion>() {
            @Override
            public void onResponse(@NonNull Call<AdminQuestion> call, @NonNull Response<AdminQuestion> response) {
                if (isFinishing()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(QuestionsManagementActivity.this, "Succès", Toast.LENGTH_SHORT).show();
                    loadQuestions();
                } else {
                    setLoading(false);
                    Log.e(TAG, "Save failed: " + response.code());
                    Toast.makeText(QuestionsManagementActivity.this, "Erreur serveur: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminQuestion> call, @NonNull Throwable t) {
                if (isFinishing()) return;
                setLoading(false);
                Toast.makeText(QuestionsManagementActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleDeactivate(AdminQuestion q) {
        setLoading(true);
        adminApi.deactivateQuestion(q.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (isFinishing()) return;
                if (response.isSuccessful()) {
                    loadQuestions();
                } else {
                    setLoading(false);
                    Toast.makeText(QuestionsManagementActivity.this, "Action échouée", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isFinishing()) return;
                setLoading(false);
                Toast.makeText(QuestionsManagementActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        swipeRefresh.setRefreshing(isLoading);
    }
}

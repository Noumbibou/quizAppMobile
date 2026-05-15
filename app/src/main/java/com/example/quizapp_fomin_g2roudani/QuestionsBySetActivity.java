package com.example.quizapp_fomin_g2roudani;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.quizapp_fomin_g2roudani.adapters.QuestionAdapter;
import com.example.quizapp_fomin_g2roudani.auth.AuthTokenManager;
import com.example.quizapp_fomin_g2roudani.models.AdminQuestion;
import com.example.quizapp_fomin_g2roudani.models.ImportResponse;
import com.example.quizapp_fomin_g2roudani.models.QuestionCreateRequest;
import com.example.quizapp_fomin_g2roudani.models.QuestionUpdateRequest;
import com.example.quizapp_fomin_g2roudani.network.AdminApi;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionsBySetActivity extends AppCompatActivity implements QuestionAdapter.OnQuestionClickListener {

    private static final String TAG = "QuestionsBySet";
    private int setId;
    private String setName, setLevel;

    private RecyclerView rvQuestions;
    private SwipeRefreshLayout swipeRefresh;
    private LinearProgressIndicator progressIndicator;
    private FloatingActionButton fabAdd;
    private ExtendedFloatingActionButton btnImportExcel;
    private QuestionAdapter adapter;
    private final List<AdminQuestion> questionList = new ArrayList<>();
    private AdminApi adminApi;

    // ✅ File Picker pour Excel
    private final ActivityResultLauncher<String> excelPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uploadExcelFile(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthTokenManager.isAdmin()) {
            finish();
            return;
        }

        setId = getIntent().getIntExtra("set_id", -1);
        setName = getIntent().getStringExtra("set_name");
        setLevel = getIntent().getStringExtra("set_level");

        if (setId == -1) {
            finish();
            return;
        }

        setContentView(R.layout.activity_questions_by_set);

        initViews();
        setupNetwork();
        setupRecyclerView();
        setupListeners();

        loadQuestions();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarSetQuestions);
        setSupportActionBar(toolbar);
        toolbar.setTitle(setName);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvQuestions = findViewById(R.id.rvSetQuestions);
        swipeRefresh = findViewById(R.id.swipeRefreshSetQuestions);
        progressIndicator = findViewById(R.id.progressSetQuestions);
        fabAdd = findViewById(R.id.fabAddQuestionToSet);
        btnImportExcel = findViewById(R.id.btnImportExcel);
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
        // ✅ On affiche d'abord les instructions avant d'ouvrir le sélecteur
        btnImportExcel.setOnClickListener(v -> showImportInstructionsDialog());
    }

    /**
     * ✅ Affiche les consignes de structure avant de permettre le choix du fichier.
     */
    private void showImportInstructionsDialog() {
        String message = "Pour réussir l'importation, votre fichier Excel (.xlsx) doit contenir exactement ces colonnes dans cet ordre :\n\n" +
                "level | question | optionA | optionB | optionC | optionD | correct\n\n" +
                "⚠️ Attention : Respectez la casse des noms de colonnes et ne laissez aucune cellule vide.";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Structure du fichier Excel")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("J'ai compris, choisir le fichier", (dialog, which) -> {
                    excelPickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void loadQuestions() {
        setLoading(true);
        adminApi.getQuestionsBySet(setId).enqueue(new Callback<List<AdminQuestion>>() {
            @Override
            public void onResponse(@NonNull Call<List<AdminQuestion>> call, @NonNull Response<List<AdminQuestion>> response) {
                if (isFinishing()) return;
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    questionList.clear();
                    questionList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AdminQuestion>> call, @NonNull Throwable t) {
                if (isFinishing()) return;
                setLoading(false);
                Toast.makeText(QuestionsBySetActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Logique d'upload Excel
    private void uploadExcelFile(Uri fileUri) {
        try {
            setLoading(true);
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            RequestBody requestFile = RequestBody.create(
                    buffer,
                    MediaType.parse(getContentResolver().getType(fileUri))
            );

            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "questions.xlsx", requestFile);

            adminApi.importQuestions(setId, body).enqueue(new Callback<ImportResponse>() {
                @Override
                public void onResponse(@NonNull Call<ImportResponse> call, @NonNull Response<ImportResponse> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        showImportResult(response.body());
                        loadQuestions();
                    } else {
                        Toast.makeText(QuestionsBySetActivity.this, "Erreur lors de l'import (422/500)", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ImportResponse> call, @NonNull Throwable t) {
                    setLoading(false);
                    Toast.makeText(QuestionsBySetActivity.this, "Serveur inaccessible", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "File read error", e);
            setLoading(false);
            Toast.makeText(this, "Impossible de lire le fichier", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImportResult(ImportResponse res) {
        StringBuilder msg = new StringBuilder();
        msg.append("Lignes traitées : ").append(res.getTotalRows()).append("\n");
        msg.append("Importées avec succès : ").append(res.getImported()).append("\n");
        msg.append("Échecs : ").append(res.getFailed());

        if (!res.getErrors().isEmpty()) {
            msg.append("\n\nDétails des erreurs :");
            for (ImportResponse.ImportError err : res.getErrors()) {
                msg.append("\n- Ligne ").append(err.getRow()).append(" : ").append(err.getMessage());
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Rapport d'importation Excel")
                .setMessage(msg.toString())
                .setPositiveButton("Fermer", null)
                .show();
    }

    @Override
    public void onQuestionClick(AdminQuestion question) {
        String[] options = {"Modifier", question.isActive() ? "Désactiver" : "Réactiver"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Actions")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showQuestionDialog(question);
                    else handleDeactivate(question);
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

        spinnerCorrect.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{"A", "B", "C", "D"}));
        spinnerLevel.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{"beginner", "intermediate", "advanced"}));

        if (questionToEdit != null) {
            etQuestion.setText(questionToEdit.getQuestion());
            etA.setText(questionToEdit.getOptionA());
            etB.setText(questionToEdit.getOptionB());
            etC.setText(questionToEdit.getOptionC());
            etD.setText(questionToEdit.getOptionD());
            spinnerCorrect.setText(questionToEdit.getCorrectOption(), false);
            spinnerLevel.setText(questionToEdit.getLevel(), false);
        } else {
            spinnerLevel.setText(setLevel, false);
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
                    q.setSetId(setId);

                    if (validateForm(q)) saveQuestion(q, questionToEdit == null);
                    else Toast.makeText(this, "Champs manquants", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private boolean validateForm(AdminQuestion q) {
        return !TextUtils.isEmpty(q.getQuestion()) && !TextUtils.isEmpty(q.getOptionA()) &&
               !TextUtils.isEmpty(q.getOptionB()) && !TextUtils.isEmpty(q.getOptionC()) &&
               !TextUtils.isEmpty(q.getOptionD()) && !TextUtils.isEmpty(q.getCorrectOption());
    }

    private void saveQuestion(AdminQuestion q, boolean isNew) {
        setLoading(true);
        if (isNew) {
            QuestionCreateRequest req = new QuestionCreateRequest(q.getQuestion(), q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(), q.getCorrectOption(), q.getLevel(), setId);
            adminApi.createQuestionInSet(setId, req).enqueue(new Callback<AdminQuestion>() {
                @Override
                public void onResponse(@NonNull Call<AdminQuestion> call, @NonNull Response<AdminQuestion> response) {
                    if (response.isSuccessful()) loadQuestions();
                    else setLoading(false);
                }
                @Override
                public void onFailure(@NonNull Call<AdminQuestion> call, @NonNull Throwable t) { setLoading(false); }
            });
        } else {
            QuestionUpdateRequest req = new QuestionUpdateRequest(q.getQuestion(), q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(), q.getCorrectOption(), q.getLevel());
            adminApi.updateQuestion(q.getId(), req).enqueue(new Callback<AdminQuestion>() {
                @Override
                public void onResponse(@NonNull Call<AdminQuestion> call, @NonNull Response<AdminQuestion> response) {
                    if (response.isSuccessful()) loadQuestions();
                    else setLoading(false);
                }
                @Override
                public void onFailure(@NonNull Call<AdminQuestion> call, @NonNull Throwable t) { setLoading(false); }
            });
        }
    }

    private void handleDeactivate(AdminQuestion q) {
        setLoading(true);
        adminApi.deactivateQuestion(q.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) loadQuestions();
                else setLoading(false);
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) { setLoading(false); }
        });
    }

    private void setLoading(boolean isLoading) {
        progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        swipeRefresh.setRefreshing(isLoading);
    }
}

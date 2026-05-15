package com.example.quizapp_fomin_g2roudani;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.quizapp_fomin_g2roudani.models.Question;
import com.example.quizapp_fomin_g2roudani.models.QuizResponse;
import com.example.quizapp_fomin_g2roudani.models.ScoreModel;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.example.quizapp_fomin_g2roudani.network.QuizApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Quiz extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "SECURE_QUIZ";
    private static final int PERMISSION_CODE = 1001;

    private View startOverlay, quizMainContent;
    private TextView tvLevel, tvCounter, tvQuestion, tvTimer;
    private MaterialButton btnA, btnB, btnC, btnD, btnNext, btnRealStart;
    private LinearProgressIndicator progressBar;
    private CircularProgressIndicator loader, timerProgress;

    private final List<Map<String, Object>> userAnswers = new ArrayList<>();
    private final List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private String level, sessionKey, userSelectedAnswer = "";

    // ✅ États de sécurité
    private boolean isQuizActive = false;
    private boolean cheatDetected = false;
    private boolean cheatAlertShown = false;
    private long startTime;
    private double latitude = 0.0, longitude = 0.0;
    private boolean cameraActive = false;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 🔒 PROTECTION 1 : Bloque les captures d'écran et l'enregistrement vidéo
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_quiz);

        level = getIntent().getStringExtra(SelectLevel.EXTRA_LEVEL);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupNavigation(); // 🔒 PROTECTION 2 : Bloque le bouton retour
        bindViews();
        fetchQuestions(level);
        checkDisplayMirroring(); // 🔒 PROTECTION 3 : Détecte les écrans externes
    }

    private void setupNavigation() {
        // ✅ BLOCAGE RÉEL ET SYSTÉMATIQUE DU BOUTON RETOUR
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isQuizActive || cheatDetected) {
                    Toast.makeText(Quiz.this, "🔒 Action interdite pendant l'examen.", Toast.LENGTH_SHORT).show();
                } else {
                    setEnabled(false);
                    onBackPressed();
                }
            }
        });
    }

    // 🔒 PROTECTION 4 : Détecte si l'étudiant projette son écran
    private void checkDisplayMirroring() {
        DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = dm.getDisplays();
        if (displays.length > 1) {
            cheatDetected = true;
            Log.e(TAG, "TRICHE : Écran secondaire détecté");
        }
    }

    private void bindViews() {
        startOverlay = findViewById(R.id.startOverlay);
        quizMainContent = findViewById(R.id.quizMainContent);
        btnRealStart = findViewById(R.id.btnRealStart);
        tvLevel = findViewById(R.id.tvLevel);
        tvCounter = findViewById(R.id.tvCounter);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvTimer = findViewById(R.id.tvTimer);
        btnA = findViewById(R.id.btnA);
        btnB = findViewById(R.id.btnB);
        btnC = findViewById(R.id.btnC);
        btnD = findViewById(R.id.btnD);
        btnNext = findViewById(R.id.btnNext);
        progressBar = findViewById(R.id.progressBar);
        loader = findViewById(R.id.loader);
        timerProgress = findViewById(R.id.timerProgress);

        btnRealStart.setOnClickListener(v -> checkPermissionsAndStart());
        btnA.setOnClickListener(v -> onOptionClicked("A", btnA));
        btnB.setOnClickListener(v -> onOptionClicked("B", btnB));
        btnC.setOnClickListener(v -> onOptionClicked("C", btnC));
        btnD.setOnClickListener(v -> onOptionClicked("D", btnD));
        btnNext.setOnClickListener(v -> goNext());
    }

    private void checkPermissionsAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA}, PERMISSION_CODE);
        } else {
            startQuizNow();
        }
    }

    private void startQuizNow() {
        startOverlay.setVisibility(View.GONE);
        quizMainContent.setVisibility(View.VISIBLE);
        
        isQuizActive = true;
        startTime = System.currentTimeMillis() / 1000;
        cameraActive = true;
        
        setupMap();
        updateUserLocation();
        showQuestion(0);
        
        if (cheatDetected) showCheatAlert();
    }

    // 🔒 PROTECTION 5 : Détection de changement d'application
    @Override
    protected void onPause() {
        super.onPause();
        if (isQuizActive && !cheatDetected) {
            cheatDetected = true;
            isQuizActive = false; // Bloque le quiz
            stopCountdown();
            Log.w(TAG, "Triche détectée : application perdue de vue.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cheatDetected && !cheatAlertShown) {
            cheatAlertShown = true;
            quizMainContent.setVisibility(View.GONE); // Cache les questions
            showCheatAlert();
        }
    }

    private void showCheatAlert() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("⚠️ TENTATIVE DE FRAUDE")
                .setMessage("Un comportement suspect a été détecté (changement d'application ou écran externe). L'examen est verrouillé et votre score sera invalidé.")
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Envoyer mon rapport", (dialog, which) -> submitFinalScore())
                .show();
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) { 
        mMap = googleMap; 
        updateUserLocation();
    }

    private void updateUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    if (mMap != null) {
                        LatLng pos = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions().position(pos).title("Position Examen"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f));
                    }
                }
            });
        }
    }

    private void fetchQuestions(String lvl) {
        showLoading(true);
        ApiClient.getClient().create(QuizApi.class).getQuestions(lvl).enqueue(new Callback<QuizResponse>() {
            @Override
            public void onResponse(Call<QuizResponse> call, Response<QuizResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    sessionKey = response.body().getSessionKey();
                    questions.addAll(response.body().getQuestions());
                    progressBar.setMax(questions.size());
                    btnRealStart.setEnabled(true);
                }
            }
            @Override public void onFailure(Call<QuizResponse> call, Throwable t) { showLoading(false); finish(); }
        });
    }

    private void showQuestion(int index) {
        if (!isQuizActive || index >= questions.size()) return;
        userSelectedAnswer = "";
        btnNext.setEnabled(false);
        resetOptionsStyle();

        Question q = questions.get(index);
        tvQuestion.setText(q.getQuestion());
        btnA.setText("A) " + q.getOptionA());
        btnB.setText("B) " + q.getOptionB());
        btnC.setText("C) " + q.getOptionC());
        btnD.setText("D) " + q.getOptionD());
        tvCounter.setText("Question " + (index + 1) + " / " + questions.size());
        progressBar.setProgress(index);
        startCountdown();
    }

    private void startCountdown() {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(20000, 10) {
            @Override public void onTick(long ms) {
                tvTimer.setText(String.valueOf(ms/1000));
                timerProgress.setProgress((int)(ms/10));
            }
            @Override public void onFinish() { if (isQuizActive) goNext(); }
        }.start();
    }

    private void stopCountdown() { if (countDownTimer != null) countDownTimer.cancel(); }

    private void goNext() {
        if (!isQuizActive) return; 
        
        stopCountdown();
        Map<String, Object> ans = new HashMap<>();
        ans.put("question_id", questions.get(currentQuestionIndex).getId());
        ans.put("selected", userSelectedAnswer);
        userAnswers.add(ans);
        currentQuestionIndex++;
        
        if (currentQuestionIndex >= questions.size()) submitFinalScore();
        else showQuestion(currentQuestionIndex);
    }

    private void onOptionClicked(String choice, MaterialButton btn) {
        if (!isQuizActive) return;
        userSelectedAnswer = choice;
        btnNext.setEnabled(true);
        resetOptionsStyle();
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1B5E20")));
    }

    private void resetOptionsStyle() {
        ColorStateList blue = ColorStateList.valueOf(Color.parseColor("#2962FF"));
        btnA.setBackgroundTintList(blue); btnB.setBackgroundTintList(blue);
        btnC.setBackgroundTintList(blue); btnD.setBackgroundTintList(blue);
    }

    private void submitFinalScore() {
        isQuizActive = false;
        showLoading(true);
        // 🔒 PROTECTION 7 : Mesure du temps passé
        long timeSpent = (System.currentTimeMillis() / 1000) - startTime;
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("level", level);
        payload.put("session_key", sessionKey);
        payload.put("answers", userAnswers);
        payload.put("cheated", cheatDetected);
        payload.put("time_spent", (int) timeSpent);
        payload.put("latitude", latitude);
        payload.put("longitude", longitude);
        payload.put("camera_active", cameraActive);

        ApiClient.getClient().create(QuizApi.class).submitScore(payload).enqueue(new Callback<ScoreModel>() {
            @Override
            public void onResponse(Call<ScoreModel> call, Response<ScoreModel> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Intent i = new Intent(Quiz.this, Score.class);
                    i.putExtra("score", response.body().getScore());
                    i.putExtra("total", response.body().getTotal());
                    i.putExtra(SelectLevel.EXTRA_LEVEL, level);
                    startActivity(i);
                    finish();
                }
            }
            @Override public void onFailure(Call<ScoreModel> call, Throwable t) { showLoading(false); }
        });
    }

    private void showLoading(boolean s) { if (loader != null) loader.setVisibility(s ? View.VISIBLE : View.GONE); }

    @Override
    public void onRequestPermissionsResult(int rc, @NonNull String[] p, @NonNull int[] gr) {
        super.onRequestPermissionsResult(rc, p, gr);
        if (rc == PERMISSION_CODE && gr.length > 0 && gr[0] == PackageManager.PERMISSION_GRANTED) {
            startQuizNow();
        } else {
            Toast.makeText(this, "Les permissions sont obligatoires pour passer l'examen.", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}

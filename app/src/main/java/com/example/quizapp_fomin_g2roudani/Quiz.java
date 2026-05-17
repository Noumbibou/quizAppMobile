package com.example.quizapp_fomin_g2roudani;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.quizapp_fomin_g2roudani.auth.AuthTokenManager;
import com.example.quizapp_fomin_g2roudani.models.Question;
import com.example.quizapp_fomin_g2roudani.models.QuizResponse;
import com.example.quizapp_fomin_g2roudani.models.ScoreModel;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.example.quizapp_fomin_g2roudani.network.QuizApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Quiz extends AppCompatActivity {

    private static final String TAG = "IA_FRAUD_DETECTION";
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

    // IA États
    private boolean isQuizActive = false;
    private boolean cheatDetected = false;
    private boolean cheatAlertShown = false;
    private boolean faceVerified = true;
    private Rect referenceFaceRect = null;
    private long lastAnalysisTime = 0;
    private int faceChanges = 0; // ✅ Compteur pour tolérer 3 anomalies avant arrêt
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;

    // Localisation & Timer
    private long startTime;
    private double latitude = 0.0, longitude = 0.0;
    private FusedLocationProviderClient fusedLocationClient;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthTokenManager.init(this);
        EdgeToEdge.enable(this);
        // Empêche captures d'écran et sécurise la fenêtre
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_quiz);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        cameraExecutor = Executors.newSingleThreadExecutor();

        initFaceDetector();
        setupNavigation();
        bindViews();

        String selectedLevel = getIntent().getStringExtra(SelectLevel.EXTRA_LEVEL);
        if (selectedLevel == null) {
            finish();
            return;
        }
        fetchQuestions(selectedLevel);
    }

    private void initFaceDetector() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build();
        faceDetector = FaceDetection.getClient(options);
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
        faceChanges = 0;
        startCameraAnalysis();
        updateUserLocation();
        showQuestion(0);
    }

    private void startCameraAnalysis() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, this::processImageProxy);
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Erreur CameraX", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImageProxy(ImageProxy imageProxy) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAnalysisTime < 3000) {
            imageProxy.close();
            return;
        }
        lastAnalysisTime = currentTime;
        if (imageProxy.getImage() != null) {
            InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
            faceDetector.process(image)
                    .addOnSuccessListener(faces -> {
                        analyzeFraudWithIA(faces);
                        imageProxy.close();
                    })
                    .addOnFailureListener(e -> imageProxy.close());
        }
    }

    private void analyzeFraudWithIA(List<Face> faces) {
        if (!isQuizActive) return;

        boolean currentAnomaly = false;

        if (faces.size() == 1) {
            Face face = faces.get(0);
            Rect currentRect = face.getBoundingBox();
            if (referenceFaceRect == null) {
                referenceFaceRect = currentRect;
            } else {
                float diffX = Math.abs(referenceFaceRect.centerX() - currentRect.centerX());
                float diffY = Math.abs(referenceFaceRect.centerY() - currentRect.centerY());
                if (diffX > 300 || diffY > 300) {
                    currentAnomaly = true;
                }
            }
        } else {
            currentAnomaly = true;
        }

        if (currentAnomaly) {
            faceChanges++;
        } else {
            faceChanges = 0;
        }

        if (faceChanges >= 3) {
            faceVerified = false;
            cheatDetected = true;
            runOnUiThread(this::showCheatAlert);
        }
    }

    private void triggerFraud(String reason) {
        Log.d(TAG, "Fraude détectée : " + reason);
        cheatDetected = true;
        faceVerified = false;
        runOnUiThread(this::showCheatAlert);
    }

    private void showCheatAlert() {
        if (cheatAlertShown) return;
        cheatAlertShown = true;
        isQuizActive = false;
        stopCountdown();

        new MaterialAlertDialogBuilder(this)
                .setTitle("⚠️ ALERTE FRAUDE")
                .setMessage("Un comportement anormal ou une sortie de l'application a été détecté. L'examen est interrompu.")
                .setCancelable(false)
                .setPositiveButton("Terminer", (dialog, which) -> submitFinalScore())
                .show();
    }

    private void submitFinalScore() {
        isQuizActive = false;
        showLoading(true);

        if (questions.size() > 0 && currentQuestionIndex < questions.size()) {
            boolean currentQuestionAnswered = false;
            for (Map<String, Object> ans : userAnswers) {
                if (ans.get("question_id").equals(questions.get(currentQuestionIndex).getId())) {
                    currentQuestionAnswered = true;
                    break;
                }
            }
            if (!currentQuestionAnswered) {
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("question_id", questions.get(currentQuestionIndex).getId());
                fallback.put("selected", userSelectedAnswer.isEmpty() ? null : userSelectedAnswer);
                userAnswers.add(fallback);
            }
        }

        long timeSpent = (System.currentTimeMillis() / 1000) - startTime;
        Map<String, Object> payload = new HashMap<>();
        payload.put("level", level);
        payload.put("session_key", sessionKey);
        payload.put("answers", userAnswers);
        payload.put("cheated", cheatDetected);
        payload.put("time_spent", (int) timeSpent);
        payload.put("latitude", latitude);
        payload.put("longitude", longitude);
        payload.put("camera_active", true);
        payload.put("face_verified", faceVerified);

        ApiClient.getClient().create(QuizApi.class).submitScore(payload).enqueue(new Callback<ScoreModel>() {
            @Override public void onResponse(Call<ScoreModel> call, Response<ScoreModel> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    startActivity(new Intent(Quiz.this, Score.class)
                            .putExtra("score", response.body().getScore())
                            .putExtra("total", response.body().getTotal())
                            .putExtra(SelectLevel.EXTRA_LEVEL, level));
                    finish();
                } else {
                    Toast.makeText(Quiz.this, "Erreur serveur : " + response.code(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override public void onFailure(Call<ScoreModel> call, Throwable t) {
                showLoading(false);
                finish();
            }
        });
    }

    private void fetchQuestions(String lvl) {
        level = lvl;
        ApiClient.getClient().create(QuizApi.class).getQuestions(lvl).enqueue(new Callback<QuizResponse>() {
            @Override public void onResponse(Call<QuizResponse> call, Response<QuizResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sessionKey = response.body().getSessionKey();
                    questions.addAll(response.body().getQuestions());
                    progressBar.setMax(questions.size());
                    btnRealStart.setEnabled(true);
                } else {
                    Toast.makeText(Quiz.this, "Erreur : " + response.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<QuizResponse> call, Throwable t) {
                Toast.makeText(Quiz.this, "Échec connexion", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showQuestion(int index) {
        if (index >= questions.size()) return;
        userSelectedAnswer = "";
        btnNext.setEnabled(false);
        resetButtonStyles();

        Question q = questions.get(index);
        tvQuestion.setText(q.getQuestion());
        btnA.setText(q.getOptionA());
        btnB.setText(q.getOptionB());
        btnC.setText(q.getOptionC());
        btnD.setText(q.getOptionD());
        tvCounter.setText("Question " + (index + 1) + " / " + questions.size());
        tvLevel.setText("Niveau: " + level);
        progressBar.setProgress(index + 1);
        startCountdown();
    }

    private void startCountdown() {
        if (countDownTimer != null) countDownTimer.cancel();
        timerProgress.setMax(20000);
        timerProgress.setProgress(20000);
        countDownTimer = new CountDownTimer(20000, 50) {
            @Override public void onTick(long ms) {
                tvTimer.setText(String.valueOf((int) Math.ceil(ms / 1000.0)));
                timerProgress.setProgress((int) ms);
            }
            @Override public void onFinish() {
                timerProgress.setProgress(0);
                tvTimer.setText("0");
                goNext();
            }
        }.start();
    }

    private void stopCountdown() { if (countDownTimer != null) countDownTimer.cancel(); }

    private void goNext() {
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
        userSelectedAnswer = choice;
        btnNext.setEnabled(true);
        resetButtonStyles();
        btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));
        btn.setTextColor(Color.WHITE);
        btn.setStrokeWidth(0);
    }

    private void resetButtonStyles() {
        MaterialButton[] buttons = {btnA, btnB, btnC, btnD};
        for (MaterialButton b : buttons) {
            b.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            b.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
            b.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.outline)));
            b.setStrokeWidth(4);
        }
    }

    private void updateUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            });
        }
    }
    private void setupNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { Toast.makeText(Quiz.this, "🔒 Action interdite.", Toast.LENGTH_SHORT).show(); }
        });
    }
    private void showLoading(boolean s) { if (loader != null) loader.setVisibility(s ? View.VISIBLE : View.GONE); }

    @Override public void onRequestPermissionsResult(int rc, @NonNull String[] p, @NonNull int[] gr) {
        super.onRequestPermissionsResult(rc, p, gr);
        if (rc == PERMISSION_CODE && gr.length > 0 && gr[0] == PackageManager.PERMISSION_GRANTED) startQuizNow();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isQuizActive && !hasFocus && !cheatDetected) {
            triggerFraud("Changement d'onglet ou perte de focus");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isQuizActive && !cheatDetected) {
            triggerFraud("Application en arrière-plan");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isQuizActive && !cheatDetected) {
            triggerFraud("Application arrêtée");
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (faceDetector != null) faceDetector.close();
    }
}
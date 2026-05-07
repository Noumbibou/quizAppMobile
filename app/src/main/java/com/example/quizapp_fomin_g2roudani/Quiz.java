package com.example.quizapp_fomin_g2roudani;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp_fomin_g2roudani.models.Question;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Quiz extends AppCompatActivity {

    private TextView tvLevel, tvCounter, tvQuestion, tvTimer;
    private MaterialButton btnA, btnB, btnC, btnD, btnNext;
    private LinearProgressIndicator progressBar;
    private CircularProgressIndicator loader;
    private CircularProgressIndicator timerProgress;
    private View quizContainer, timerContainer;

    private final List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String level;
    private String userSelectedAnswer = "";

    private CountDownTimer countDownTimer;
    private static final long TIME_LIMIT = 20000; // 20 secondes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz);

        // Gestion du bouton retour avec OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        bindViews();

        if (getIntent() != null) {
            level = getIntent().getStringExtra(SelectLevel.EXTRA_LEVEL);
        }

        if (TextUtils.isEmpty(level)) {
            Toast.makeText(this, "Niveau introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvLevel.setText("Niveau: " + levelLabel(level));
        fetchQuestions(level);
    }

    private void bindViews() {
        tvLevel = findViewById(R.id.tvLevel);
        tvCounter = findViewById(R.id.tvCounter);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvTimer = findViewById(R.id.tvTimer);
        quizContainer = findViewById(R.id.quizContainer);
        timerContainer = findViewById(R.id.timerContainer);
        timerProgress = findViewById(R.id.timerProgress);

        btnA = findViewById(R.id.btnA);
        btnB = findViewById(R.id.btnB);
        btnC = findViewById(R.id.btnC);
        btnD = findViewById(R.id.btnD);
        btnNext = findViewById(R.id.btnNext);

        progressBar = findViewById(R.id.progressBar);
        loader = findViewById(R.id.loader);

        btnA.setOnClickListener(v -> onOptionClicked("A", btnA));
        btnB.setOnClickListener(v -> onOptionClicked("B", btnB));
        btnC.setOnClickListener(v -> onOptionClicked("C", btnC));
        btnD.setOnClickListener(v -> onOptionClicked("D", btnD));

        btnNext.setOnClickListener(v -> goNext());
    }

    private void startCountdown() {
        if (countDownTimer != null) countDownTimer.cancel();

        timerProgress.setIndicatorColor(Color.parseColor("#2962FF")); // Primary Blue
        tvTimer.setTextColor(Color.parseColor("#2962FF"));

        countDownTimer = new CountDownTimer(TIME_LIMIT, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                tvTimer.setText(String.valueOf(seconds));
                timerProgress.setProgress((int) (millisUntilFinished / 10));

                if (seconds <= 5) {
                    timerProgress.setIndicatorColor(Color.RED);
                    tvTimer.setTextColor(Color.RED);
                }
            }

            @Override
            public void onFinish() {
                tvTimer.setText("0");
                timerProgress.setProgress(0);

                Animation shake = AnimationUtils.loadAnimation(Quiz.this, R.anim.shake);
                if (timerContainer != null) {
                    timerContainer.startAnimation(shake);
                }

                Toast.makeText(Quiz.this, "Temps écoulé !", Toast.LENGTH_SHORT).show();
                goNext();
            }
        }.start();
    }

    private void stopCountdown() {
        if (countDownTimer != null) countDownTimer.cancel();
    }

    private void goNext() {
        stopCountdown();
        
        if (questions.isEmpty() || currentQuestionIndex >= questions.size()) return;

        Question q = questions.get(currentQuestionIndex);
        if (!TextUtils.isEmpty(userSelectedAnswer) &&
                userSelectedAnswer.equalsIgnoreCase(q.getCorrectAnswer())) {
            score++;
        }

        currentQuestionIndex++;

        if (currentQuestionIndex >= questions.size()) {
            Intent i = new Intent(this, Score.class);
            i.putExtra("score", score);
            i.putExtra("total", questions.size());
            i.putExtra(SelectLevel.EXTRA_LEVEL, level);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        } else {
            quizContainer.animate().alpha(0f).translationX(-100f).setDuration(250).withEndAction(() -> {
                showQuestion(currentQuestionIndex);
                quizContainer.setTranslationX(100f);
                quizContainer.animate().alpha(1f).translationX(0f).setDuration(250).start();
            }).start();
        }
    }

    private void onOptionClicked(String userChoice, MaterialButton clickedBtn) {
        stopCountdown();
        userSelectedAnswer = userChoice;

        resetButtonStyle(btnA);
        resetButtonStyle(btnB);
        resetButtonStyle(btnC);
        resetButtonStyle(btnD);

        clickedBtn.animate().scaleX(1.05f).scaleY(1.05f).setDuration(100).withEndAction(() ->
                clickedBtn.animate().scaleX(1.0f).scaleY(1.0f).start()
        ).start();

        clickedBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1B5E20"))); // Success Green
        clickedBtn.setTextColor(Color.WHITE);

        if (!btnNext.isEnabled()) {
            btnNext.setEnabled(true);
            btnNext.setAlpha(0f);
            btnNext.setTranslationY(20f);
            btnNext.animate().alpha(1f).translationY(0f).setDuration(300).start();
        }
    }

    private String levelLabel(String lvl) {
        if (lvl == null) return "";
        switch (lvl) {
            case SelectLevel.LEVEL_BEGINNER: return "Débutant";
            case SelectLevel.LEVEL_INTERMEDIATE: return "Intermédiaire";
            case SelectLevel.LEVEL_ADVANCED: return "Avancé";
            default: return lvl;
        }
    }

    private void fetchQuestions(String lvl) {
        showLoading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference qRef = db.collection("questions").document(lvl).collection("qList");

        qRef.get().addOnCompleteListener(task -> {
            showLoading(false);
            if (!task.isSuccessful() || task.getResult() == null) {
                Toast.makeText(this, "Erreur lors du chargement des questions", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            List<QueryDocumentSnapshot> documents = new ArrayList<>();
            for (QueryDocumentSnapshot document : task.getResult()) {
                documents.add(document);
            }

            Collections.sort(documents, Comparator.comparing(QueryDocumentSnapshot::getId, (a, b) -> {
                try {
                    int ai = Integer.parseInt(a.replaceAll("\\D+", ""));
                    int bi = Integer.parseInt(b.replaceAll("\\D+", ""));
                    return Integer.compare(ai, bi);
                } catch (Exception e) { return a.compareTo(b); }
            }));

            questions.clear();
            for (QueryDocumentSnapshot d : documents) {
                Question q = d.toObject(Question.class);
                if (q != null && q.getCorrectAnswer() != null) {
                    questions.add(q);
                }
            }

            if (questions.isEmpty()) {
                Toast.makeText(this, "Aucune question trouvée", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                progressBar.setMax(questions.size());
                showQuestion(0);
            }
        });
    }

    private void showLoading(boolean show) {
        if (loader != null) {
            loader.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        
        userSelectedAnswer = "";
        btnNext.setEnabled(false);

        resetButtonStyle(btnA);
        resetButtonStyle(btnB);
        resetButtonStyle(btnC);
        resetButtonStyle(btnD);

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

    private void resetButtonStyle(MaterialButton b) {
        if (b == null) return;
        b.setEnabled(true);
        b.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2962FF"))); // Primary Blue
        b.setTextColor(Color.WHITE);
        b.setStrokeWidth(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCountdown();
    }
}
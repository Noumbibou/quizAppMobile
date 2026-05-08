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

import com.example.quizapp_fomin_g2roudani.auth.AuthTokenManager;
import com.example.quizapp_fomin_g2roudani.models.Question;
import com.example.quizapp_fomin_g2roudani.models.ScoreModel;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.example.quizapp_fomin_g2roudani.network.QuizApi;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Quiz extends AppCompatActivity {

    private TextView tvLevel, tvCounter, tvQuestion, tvTimer;
    private MaterialButton btnA, btnB, btnC, btnD, btnNext;
    private LinearProgressIndicator progressBar;
    private CircularProgressIndicator loader, timerProgress;
    private View quizContainer, timerContainer;
    private final List<Map<String, Object>> userAnswers = new ArrayList<>();
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

        // Gestion du bouton retour
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finish();
                        overridePendingTransition(
                                R.anim.slide_in_left,
                                R.anim.slide_out_right
                        );
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

        tvLevel.setText("Niveau : " + levelLabel(level));

        // ✅ Chargement des questions depuis FastAPI
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

    // ---------------------------------------------------------------------
    // 🔥 Récupération des questions depuis FastAPI
    // ---------------------------------------------------------------------
    private void fetchQuestions(String lvl) {
        showLoading(true);

        // ✅ UTILISATION DU TOKEN EXISTANT (PAS DE REGENERATION)
        if (!AuthTokenManager.hasToken()) {
            showLoading(false);
            Toast.makeText(this,
                    "Session expirée. Veuillez vous reconnecter.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String token = AuthTokenManager.getToken();

        // ✅ DEBUG TEMPORAIRE
        android.util.Log.e("QUIZ_TOKEN_USED", token);

        Retrofit retrofit = ApiClient.getClient();
        QuizApi quizApi = retrofit.create(QuizApi.class);

        quizApi.getQuestions(lvl)
                .enqueue(new Callback<List<Question>>() {

                    @Override
                    public void onResponse(
                            Call<List<Question>> call,
                            Response<List<Question>> response) {

                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {

                            questions.clear();
                            questions.addAll(response.body());

                            if (questions.isEmpty()) {
                                Toast.makeText(
                                        Quiz.this,
                                        "Aucune question trouvée",
                                        Toast.LENGTH_SHORT
                                ).show();
                                finish();
                            } else {
                                progressBar.setMax(questions.size());
                                showQuestion(0);
                            }

                        } else {
                            Toast.makeText(
                                    Quiz.this,
                                    "Erreur serveur (" + response.code() + ")",
                                    Toast.LENGTH_LONG
                            ).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<Question>> call,
                            Throwable t) {

                        showLoading(false);

                        Toast.makeText(
                                Quiz.this,
                                "Impossible de contacter le serveur",
                                Toast.LENGTH_LONG
                        ).show();
                        finish();
                    }
                });
    }
    // ---------------------------------------------------------------------
    // 🧠 Affichage des questions
    // ---------------------------------------------------------------------
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

        tvCounter.setText(
                "Question " + (index + 1) + " / " + questions.size()
        );

        progressBar.setProgress(index);
        startCountdown();
    }

    // ---------------------------------------------------------------------
    // ⏱️ Timer
    // ---------------------------------------------------------------------
    private void startCountdown() {
        if (countDownTimer != null) countDownTimer.cancel();

        timerProgress.setIndicatorColor(Color.parseColor("#2962FF"));
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

                Animation shake =
                        AnimationUtils.loadAnimation(
                                Quiz.this,
                                R.anim.shake
                        );
                if (timerContainer != null) {
                    timerContainer.startAnimation(shake);
                }

                Toast.makeText(
                        Quiz.this,
                        "Temps écoulé !",
                        Toast.LENGTH_SHORT
                ).show();
                goNext();
            }
        }.start();
    }

    private void stopCountdown() {
        if (countDownTimer != null) countDownTimer.cancel();
    }

    // ---------------------------------------------------------------------
    // ✅ Choix de réponse
    // ---------------------------------------------------------------------
    private void onOptionClicked(
            String userChoice,
            MaterialButton clickedBtn) {

        stopCountdown();
        userSelectedAnswer = userChoice;

        resetButtonStyle(btnA);
        resetButtonStyle(btnB);
        resetButtonStyle(btnC);
        resetButtonStyle(btnD);

        clickedBtn.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(100)
                .withEndAction(() ->
                        clickedBtn.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .start())
                .start();

        clickedBtn.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#1B5E20")));
        clickedBtn.setTextColor(Color.WHITE);

        if (!btnNext.isEnabled()) {
            btnNext.setEnabled(true);
            btnNext.setAlpha(0f);
            btnNext.setTranslationY(20f);
            btnNext.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start();
        }
    }

    // ---------------------------------------------------------------------
    // ▶️ Question suivante
    // ---------------------------------------------------------------------
    private void goNext() {
        stopCountdown();

        if (questions.isEmpty()
                || currentQuestionIndex >= questions.size()) return;


        Question q = questions.get(currentQuestionIndex);

        Map<String, Object> answer = new HashMap<>();
        answer.put("question_id", q.getId());   // ⚠️ l’id MySQL
        answer.put("selected", userSelectedAnswer);
        userAnswers.add(answer);


        currentQuestionIndex++;

        if (currentQuestionIndex >= questions.size()) {
// ✅ Construire le payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("level", level);
            payload.put("answers", userAnswers);

            Retrofit retrofit = ApiClient.getClient();
            QuizApi quizApi = retrofit.create(QuizApi.class);

            quizApi.submitScore(payload).enqueue(new Callback<ScoreModel>() {

                @Override
                public void onResponse(Call<ScoreModel> call,
                                       Response<ScoreModel> response) {

                    if (response.isSuccessful() && response.body() != null) {

                        ScoreModel scoreResponse = response.body();

                        Intent i = new Intent(Quiz.this, Score.class);
                        i.putExtra("score", scoreResponse.getScore());
                        i.putExtra("total", scoreResponse.getTotal());
                        i.putExtra(SelectLevel.EXTRA_LEVEL, level);

                        startActivity(i);
                        overridePendingTransition(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                        );
                        finish();
                    } else {
                        Toast.makeText(Quiz.this,
                                "Erreur calcul du score",
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ScoreModel> call, Throwable t) {
                    Toast.makeText(Quiz.this,
                            "Impossible d'envoyer le score",
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            quizContainer.animate()
                    .alpha(0f)
                    .translationX(-100f)
                    .setDuration(250)
                    .withEndAction(() -> {
                        showQuestion(currentQuestionIndex);
                        quizContainer.setTranslationX(100f);
                        quizContainer.animate()
                                .alpha(1f)
                                .translationX(0f)
                                .setDuration(250)
                                .start();
                    }).start();
        }
    }

    private String levelLabel(String lvl) {
        if (lvl == null) return "";
        switch (lvl) {
            case SelectLevel.LEVEL_BEGINNER:
                return "Débutant";
            case SelectLevel.LEVEL_INTERMEDIATE:
                return "Intermédiaire";
            case SelectLevel.LEVEL_ADVANCED:
                return "Avancé";
            default:
                return lvl;
        }
    }

    private void showLoading(boolean show) {
        if (loader != null) {
            loader.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void resetButtonStyle(MaterialButton b) {
        if (b == null) return;
        b.setEnabled(true);
        b.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#2962FF")));
        b.setTextColor(Color.WHITE);
        b.setStrokeWidth(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCountdown();
    }
}

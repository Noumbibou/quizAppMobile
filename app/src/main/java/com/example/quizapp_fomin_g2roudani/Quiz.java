package com.example.quizapp_fomin_g2roudani;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp_fomin_g2roudani.models.Question;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Quiz extends AppCompatActivity {

    private TextView tvLevel, tvCounter, tvQuestion;
    private MaterialButton btnA, btnB, btnC, btnD, btnNext;
    private ProgressBar progressBar, loader;

    private final List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String level;

    // NOTE: on n’interdit plus de recliquer. On garde simplement la DERNIÈRE réponse choisie.
    private String userSelectedAnswer = ""; // "A" | "B" | "C" | "D"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz);

        bindViews();

        level = getIntent().getStringExtra(SelectLevel.EXTRA_LEVEL);
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

        btnA = findViewById(R.id.btnA);
        btnB = findViewById(R.id.btnB);
        btnC = findViewById(R.id.btnC);
        btnD = findViewById(R.id.btnD);
        btnNext = findViewById(R.id.btnNext);

        progressBar = findViewById(R.id.progressBar);
        loader = findViewById(R.id.loader);

        // NOTE: on autorise le changement de réponse => pas de blocage
        btnA.setOnClickListener(v -> onOptionClicked("A", btnA));
        btnB.setOnClickListener(v -> onOptionClicked("B", btnB));
        btnC.setOnClickListener(v -> onOptionClicked("C", btnC));
        btnD.setOnClickListener(v -> onOptionClicked("D", btnD));

        btnNext.setOnClickListener(v -> goNext());
    }

    private void goNext() {
        // NOTE: on compte le point uniquement à la validation (Suivant)
        Question q = questions.get(currentQuestionIndex);
        if (!TextUtils.isEmpty(userSelectedAnswer) &&
                userSelectedAnswer.equalsIgnoreCase(q.getCorrectAnswer())) {
            score++;
        }

        currentQuestionIndex++;

        if (currentQuestionIndex >= questions.size()) {
            // Fin du quiz
            Intent i = new Intent(this, Score.class);
            i.putExtra("score", score);
            i.putExtra("total", questions.size());
            i.putExtra(SelectLevel.EXTRA_LEVEL, level);
            startActivity(i);
            finish();
        } else {
            showQuestion(currentQuestionIndex);
        }
    }

    private void onOptionClicked(String userChoice, MaterialButton clickedBtn) {
        // NOTE: On mémorise simplement la DERNIÈRE réponse choisie
        userSelectedAnswer = userChoice;

        // Reset des styles (enlever le vert précédent)
        resetButtonStyle(btnA);
        resetButtonStyle(btnB);
        resetButtonStyle(btnC);
        resetButtonStyle(btnD);

        // NOTE: couleur verte quand sélectionné avec texte blanc
        clickedBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#22C55E"))); 
        clickedBtn.setTextColor(Color.WHITE);

        // NOTE: activer "Suivant" dès qu’un choix est fait
        btnNext.setEnabled(true);
    }

    private String levelLabel(String lvl) {
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

            // NOTE: tri par id q1..q50 si présent
            Collections.sort(
                    documents,
                    Comparator.comparing(
                            QueryDocumentSnapshot::getId,
                            (a, b) -> {
                                try {
                                    int ai = Integer.parseInt(a.replaceAll("\\D+", ""));
                                    int bi = Integer.parseInt(b.replaceAll("\\D+", ""));
                                    return Integer.compare(ai, bi);
                                } catch (Exception e) {
                                    return a.compareTo(b);
                                }
                            }
                    )
            );

            for (QueryDocumentSnapshot d : documents) {
                Question q = d.toObject(Question.class);
                if (q.getCorrectAnswer() == null) continue; // sécurité
                questions.add(q);
            }

            // NOTE: Cette partie était dans la boucle chez toi -> bug de multiples appels
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
        loader.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void showQuestion(int index) {
        // Reset état sélection et UI
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
    }

    private void resetButtonStyle(MaterialButton b) {
        b.setEnabled(true);
        // Force le bleu primaire et texte blanc au lieu du noir/gris
        b.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1E40AF"))); 
        b.setTextColor(Color.WHITE);
        b.setStrokeWidth(0);
    }
}
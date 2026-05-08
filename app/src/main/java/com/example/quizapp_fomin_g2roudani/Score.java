package com.example.quizapp_fomin_g2roudani;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class Score extends AppCompatActivity {

    private TextView tvScoreValue, tvPercentage, tvLevelPlayed;
    private MaterialButton btnRetry, btnBack, btnLeaderboard, btnMyScores;
    private CircularProgressIndicator scoreCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        // Initialisation des vues
        tvScoreValue = findViewById(R.id.tvScoreValue);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvLevelPlayed = findViewById(R.id.tvLevelPlayed);
        btnRetry = findViewById(R.id.btnRetry);
        btnBack = findViewById(R.id.btnBack);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnMyScores = findViewById(R.id.btnMyScores);
        scoreCircle = findViewById(R.id.scoreCircle);

        // Récupération des données du quiz
        int score = getIntent().getIntExtra("score", 0);
        int total = getIntent().getIntExtra("total", 0);
        String level = getIntent().getStringExtra(SelectLevel.EXTRA_LEVEL);

        if (total == 0) total = 1; // Éviter division par zéro
        int percent = Math.round((score * 100f) / total);

        // Affichage des résultats
        tvScoreValue.setText("Score : " + score + " / " + total);
        tvPercentage.setText(percent + "%");
        tvLevelPlayed.setText("Niveau : " + formatLevel(level));
        
        // Mise à jour visuelle du cercle
        scoreCircle.setProgress(percent);

        // Configuration des listeners
        btnRetry.setOnClickListener(v -> {
            Intent intent = new Intent(this, Quiz.class);
            intent.putExtra(SelectLevel.EXTRA_LEVEL, level);
            startActivity(intent);
            finish();
        });

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, SelectLevel.class));
            finish();
        });

        btnLeaderboard.setOnClickListener(v -> {
            startActivity(new Intent(this, LeaderboardActivity.class));
        });

        btnMyScores.setOnClickListener(v -> {
            startActivity(new Intent(this, MyScoresActivity.class));
        });
    }

    private String formatLevel(String level) {
        if (level == null) return "-";
        switch (level) {
            case SelectLevel.LEVEL_BEGINNER: return "Débutant";
            case SelectLevel.LEVEL_INTERMEDIATE: return "Intermédiaire";
            case SelectLevel.LEVEL_ADVANCED: return "Avancé";
            default: return level;
        }
    }
}

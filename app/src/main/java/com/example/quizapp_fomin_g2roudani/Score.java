package com.example.quizapp_fomin_g2roudani;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp_fomin_g2roudani.models.ScoreModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Score extends AppCompatActivity {

    private TextView tvScoreTitle, tvScoreValue, tvPercentage, tvLevelPlayed;
    private MaterialButton btnRetry, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_score);

        bindViews();

        //Recuperation des données envoyées depuis Quiz.java
        int score = getIntent().getIntExtra("score", 0);
        int total = getIntent().getIntExtra("total", 0);
        String level = getIntent().getStringExtra(SelectLevel.EXTRA_LEVEL);

        //Calcul pourcentage
        int percent = Math.round(score * 100f / total);

        //Mise à jour UI
        tvScoreValue.setText("Score: " + score + " / " + total);
        tvPercentage.setText(percent + "%");
        tvLevelPlayed.setText("Niveau : " + formatLevel(level));

        //Enregistrement dans fireStore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && level != null) {
            String uid = user.getUid();
            db.collection("users").document(uid).collection("scores").document(level).set(
                    new ScoreModel(score, total)).addOnSuccessListener(aVoid -> {
                // Enregistrement réussi
                Toast.makeText(this, "Score enregistré", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                // Enregistrement échoué
                Toast.makeText(this, "Erreur lors de l'enregistrement du score", Toast.LENGTH_SHORT).show();
            });
        }

        //Bouton pour recommencer le mm niveau
        btnRetry.setOnClickListener(v -> {
            Intent intent = new Intent(this, Quiz.class);
            intent.putExtra(SelectLevel.EXTRA_LEVEL, level);
            startActivity(intent);
            finish();
        });

        //Bouton pour retourner aux niveaux
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectLevel.class);
            startActivity(intent);
            finish();
        });
    }
    private void bindViews(){
        tvScoreTitle = findViewById(R.id.tvScoreTitle);
        tvScoreValue = findViewById(R.id.tvScoreValue);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvLevelPlayed = findViewById(R.id.tvLevelPlayed);
        btnRetry = findViewById(R.id.btnRetry);
        btnBack = findViewById(R.id.btnBack);
    }
    private String formatLevel(String level){
        if(level == null) return "-";
        switch (level){
            case SelectLevel.LEVEL_BEGINNER: return "Débutant";
            case SelectLevel.LEVEL_INTERMEDIATE: return "Intermédiaire";
            case SelectLevel.LEVEL_ADVANCED: return "Avancé";
            default: return level;
        }
    }
}
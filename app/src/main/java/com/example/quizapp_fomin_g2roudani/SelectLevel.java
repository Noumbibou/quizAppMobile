package com.example.quizapp_fomin_g2roudani;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;


public class SelectLevel extends AppCompatActivity {
    public static final String LEVEL_BEGINNER = "beginner";
    public static final String LEVEL_INTERMEDIATE = "intermediate";
    public static final String LEVEL_ADVANCED = "advanced";
    public static final String EXTRA_LEVEL = "level";
    private MaterialCardView cardBeginner;
    private MaterialCardView cardIntermediate;
    private MaterialCardView cardAdvanced;
    private boolean navigationInprogress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_level);

        if(!ensureAuthentificated()){
            return;
        }
        bindViews();
        binClicks();
    }
    private boolean ensureAuthentificated() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vous devez être connecté pour accéder à cette page", Toast.LENGTH_SHORT).show();
            Intent backToLogin = new Intent(this, MainActivity.class);
            backToLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(backToLogin);
            finish();
            return false;
        }
        return true;
    }

    private void bindViews(){
        cardBeginner = findViewById(R.id.cardBeginner);
        cardIntermediate = findViewById(R.id.cardIntermediate);
        cardAdvanced = findViewById(R.id.cardAdvanced);
    }
    private void binClicks(){
        View.OnClickListener listener = v -> {
            if(navigationInprogress) return;
            navigationInprogress = true;
            String level = null;
            int id = v.getId();
            if(id == R.id.cardBeginner){
                level = LEVEL_BEGINNER;
            }else if(id == R.id.cardIntermediate){
                level = LEVEL_INTERMEDIATE;
            }else if(id == R.id.cardAdvanced){
                level = LEVEL_ADVANCED;
            }else {
                navigationInprogress = false;
                return;
            }
            goToQuiz(level);
        };
        cardBeginner.setOnClickListener(listener);
        cardIntermediate.setOnClickListener(listener);
        cardAdvanced.setOnClickListener(listener);
    }
    private void goToQuiz(String level){
        Intent intent = new Intent(this, Quiz.class);
        intent.putExtra(EXTRA_LEVEL, level);
        startActivity(intent);
        navigationInprogress = false;
    }
}
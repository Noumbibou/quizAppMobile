package com.example.quizapp_fomin_g2roudani;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp_fomin_g2roudani.auth.AuthTokenManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class AdminDashboardActivity extends AppCompatActivity {

    private MaterialCardView cardManageUsers, cardManageQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ SÉCURITÉ : Vérification du rôle admin
        if (!AuthTokenManager.isAdmin()) {
            Log.e("SECURITY", "Accès non autorisé à l'AdminDashboard");
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        setupListeners();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarAdmin);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardManageQuestions = findViewById(R.id.cardManageQuestions);
    }

    private void setupListeners() {
        cardManageUsers.setOnClickListener(v -> {
            // ✅ Gestion des utilisateurs
            Intent intent = new Intent(this, UsersManagementActivity.class);
            startActivity(intent);
        });

        cardManageQuestions.setOnClickListener(v -> {
            // ✅ Nouvelle entrée : Gestion des Packs de Questions
            Intent intent = new Intent(this, QuestionSetsManagementActivity.class);
            startActivity(intent);
        });
    }
}

package com.example.quizapp_fomin_g2roudani;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.quizapp_fomin_g2roudani.models.UserUpdateRequest;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.example.quizapp_fomin_g2roudani.network.AuthApi;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private MaterialButton btnSave;
    private LinearProgressIndicator progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarEditProfile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.etEditName);
        btnSave = findViewById(R.id.btnSaveProfile);
        progressBar = findViewById(R.id.editProfileProgress);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            etName.setText(user.getDisplayName());
        }

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String newName = etName.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un nom", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        showLoading(true);

        // 1. Mettre à jour Firebase
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 2. Mettre à jour le backend
                updateBackendProfile(newName);
            } else {
                showLoading(false);
                Toast.makeText(this, "Erreur Firebase: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBackendProfile(String newName) {
        AuthApi authApi = ApiClient.getClient().create(AuthApi.class);
        authApi.updateUserProfile(new UserUpdateRequest(newName)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Erreur backend: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "Échec connexion: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
        etName.setEnabled(!loading);
    }
}

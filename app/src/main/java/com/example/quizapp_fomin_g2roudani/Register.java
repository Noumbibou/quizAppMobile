package com.example.quizapp_fomin_g2roudani;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Register extends AppCompatActivity {

    private TextInputLayout tilNom, tilEmail, tilPassword, tilConfirm;
    private TextInputEditText etNom, etMail, etPassword, etConfirm;
    private MaterialButton btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        initViews();

        btnRegister.setOnClickListener(v -> handleRegister());
    }

    private void initViews() {
        tilNom = findViewById(R.id.tilNom);
        tilEmail = findViewById(R.id.tilEmailReg);
        tilPassword = findViewById(R.id.tilPassReg);
        tilConfirm = findViewById(R.id.tilConfirmReg);

        etNom = findViewById(R.id.etNom);
        etMail = findViewById(R.id.etMail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.confirmPassword);
        btnRegister = findViewById(R.id.Bregister);
    }

    private void handleRegister() {
        String nom = etNom.getText().toString().trim();
        String email = etMail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirm.getText().toString().trim();

        // Reset errors
        tilNom.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirm.setError(null);

        if (TextUtils.isEmpty(nom)) {
            tilNom.setError(getString(R.string.error_field_required));
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email));
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_short));
            return;
        }
        if (!password.equals(confirm)) {
            tilConfirm.setError(getString(R.string.error_password_match));
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(nom)
                                .build();
                        user.updateProfile(profileUpdates);
                    }
                    Toast.makeText(this, "Compte créé !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

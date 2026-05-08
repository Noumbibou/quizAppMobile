package com.example.quizapp_fomin_g2roudani;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp_fomin_g2roudani.auth.AuthTokenManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityAuth";

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etMail, etPassword;
    private MaterialButton bLogin, btnGoogle;
    private TextView tvRegister;
    private LinearProgressIndicator progressIndicator;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.core.splashscreen.SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupGoogleSignIn();

        // ✅ AUTO‑LOGIN PROPRE (SANS APPEL BACKEND)
        if (mAuth.getCurrentUser() != null) {
            fetchTokenAndEnterApp();
        }

        bLogin.setOnClickListener(v -> onLoginClick());
        btnGoogle.setOnClickListener(v -> onGoogleClick());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, Register.class)));
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etMail = findViewById(R.id.etMail);
        etPassword = findViewById(R.id.etPassword);
        bLogin = findViewById(R.id.bLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvRegister = findViewById(R.id.tvRegister);
        progressIndicator = findViewById(R.id.loginProgress);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void onLoginClick() {
        String email = etMail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        tilEmail.setError(null);
        tilPassword.setError(null);

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email));
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_short));
            return;
        }

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> fetchTokenAndEnterApp())
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void onGoogleClick() {
        googleSignInLauncher.launch(mGoogleSignInClient.getSignInIntent());
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            try {
                                GoogleSignInAccount account =
                                        GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                                .getResult(ApiException.class);
                                firebaseAuthWithGoogle(account.getIdToken());
                            } catch (Exception e) {
                                Toast.makeText(this, "Google Sign‑In échoué", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

    private void firebaseAuthWithGoogle(String idToken) {
        setLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> fetchTokenAndEnterApp())
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Auth Google échouée", Toast.LENGTH_SHORT).show();
                });
    }

    // ✅ SEULE FONCTION D’AUTH
    // ✅ PAS D’APPEL BACKEND ICI
    private void fetchTokenAndEnterApp() {
        mAuth.getCurrentUser().getIdToken(true)
                .addOnSuccessListener(result -> {
                    AuthTokenManager.saveToken(result.getToken());
                    Log.d(TAG, "Token Firebase prêt");
                    goToApp();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Erreur Firebase", Toast.LENGTH_SHORT).show();
                });
    }

    private void goToApp() {
        setLoading(false);
        startActivity(new Intent(this, SelectLevel.class));
        finish();
    }

    private void setLoading(boolean loading) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }
}
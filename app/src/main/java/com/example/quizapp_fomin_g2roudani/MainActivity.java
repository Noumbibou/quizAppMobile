package com.example.quizapp_fomin_g2roudani;

import android.app.Activity;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp_fomin_g2roudani.auth.AuthTokenManager;
import com.example.quizapp_fomin_g2roudani.models.UserMe;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.example.quizapp_fomin_g2roudani.network.AuthApi;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityAuth";

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etMail, etPassword;
    private MaterialButton bLogin, btnGoogle;
    private TextView tvRegister;
    private LinearProgressIndicator progressIndicator;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    // ✅ Utilisation du contrat avec le chemin complet pour éviter l'erreur de constructeur
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            try {
                                GoogleSignInAccount account =
                                        GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                                .getResult(ApiException.class);

                                if (account != null) {
                                    firebaseAuthWithGoogle(account.getIdToken());
                                }
                            } catch (ApiException e) {
                                Log.e(TAG, "Google Sign-In failed", e);
                                Toast.makeText(this, "Connexion Google échouée", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.core.splashscreen.SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        AuthTokenManager.init(this);

        initViews();
        setupGoogleSignIn();

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
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void onLoginClick() {
        String email = (etMail.getText() != null) ? etMail.getText().toString().trim() : "";
        String password = (etPassword.getText() != null) ? etPassword.getText().toString().trim() : "";

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
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void onGoogleClick() {
        googleSignInLauncher.launch(mGoogleSignInClient.getSignInIntent());
    }

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

    private void fetchTokenAndEnterApp() {
        if (mAuth.getCurrentUser() == null) return;

        setLoading(true);
        mAuth.getCurrentUser().getIdToken(true)
                .addOnSuccessListener(result -> {
                    String token = result.getToken();
                    AuthTokenManager.saveToken(MainActivity.this, token);

                    // ✅ CRITIQUE : forcer Retrofit à recréer l'interceptor
                    ApiClient.invalidate();

                    AuthApi authApi = ApiClient.getClient().create(AuthApi.class);
                    authApi.getMe().enqueue(new Callback<UserMe>() {
                        @Override
                        public void onResponse(@NonNull Call<UserMe> call,
                                               @NonNull Response<UserMe> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                boolean admin = response.body().isAdmin();
                                Log.d("ADMIN_CHECK", "isAdmin: " + admin);
                                AuthTokenManager.saveAdminStatus(MainActivity.this, admin);
                                goToApp();
                            } else {
                                setLoading(false);
                                Toast.makeText(MainActivity.this,
                                        "Erreur session", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<UserMe> call,
                                              @NonNull Throwable t) {
                            setLoading(false);
                            Toast.makeText(MainActivity.this,
                                    "Serveur inaccessible", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this,
                            "Erreur d'authentification", Toast.LENGTH_SHORT).show();
                });
    }

    private void goToApp() {
        setLoading(false);
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    private void setLoading(boolean loading) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }
}
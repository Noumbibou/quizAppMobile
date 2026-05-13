package com.example.quizapp_fomin_g2roudani;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.quizapp_fomin_g2roudani.adapters.UserAdapter;
import com.example.quizapp_fomin_g2roudani.auth.AuthTokenManager;
import com.example.quizapp_fomin_g2roudani.models.AdminUser;
import com.example.quizapp_fomin_g2roudani.network.AdminApi;
import com.example.quizapp_fomin_g2roudani.network.ApiClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersManagementActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    private static final String TAG = "UsersManagement";
    private RecyclerView rvUsers;
    private SwipeRefreshLayout swipeRefresh;
    private LinearProgressIndicator progressIndicator;
    private UserAdapter adapter;
    private final List<AdminUser> userList = new ArrayList<>();
    private AdminApi adminApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ SÉCURITÉ : Vérification du rôle admin
        if (!AuthTokenManager.isAdmin()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_users_management);

        initViews();
        setupNetwork();
        setupRecyclerView();
        setupListeners();

        loadUsers();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarUsers);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvUsers = findViewById(R.id.rvUsers);
        swipeRefresh = findViewById(R.id.swipeRefreshUsers);
        progressIndicator = findViewById(R.id.progressUsers);
    }

    private void setupNetwork() {
        adminApi = ApiClient.getClient().create(AdminApi.class);
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(userList, this);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadUsers);
    }

    private void loadUsers() {
        setLoading(true);
        adminApi.getAllUsers().enqueue(new Callback<List<AdminUser>>() {
            @Override
            public void onResponse(@NonNull Call<List<AdminUser>> call, @NonNull Response<List<AdminUser>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(UsersManagementActivity.this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AdminUser>> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Load users failed", t);
                Toast.makeText(UsersManagementActivity.this, "Serveur inaccessible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUserClick(AdminUser user) {
        showUserActionsDialog(user);
    }

    private void showUserActionsDialog(AdminUser user) {
        // 1. Inflater le layout personnalisé
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_actions, null);

        TextView tvRole = dialogView.findViewById(R.id.tvUserRoleValue);
        Button btnAdmin = dialogView.findViewById(R.id.btnToggleAdmin);
        Button btnDisable = dialogView.findViewById(R.id.btnToggleDisable);

        // 2. Mise à jour dynamique du Rôle
        if (user.isAdmin()) {
            tvRole.setText("Administrateur");
            tvRole.setTextColor(getResources().getColor(R.color.primary));
            btnAdmin.setText("Retirer le rôle admin");
            btnAdmin.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);        } else {
            tvRole.setText("Utilisateur");
            tvRole.setTextColor(getResources().getColor(R.color.on_surface));
            btnAdmin.setText("Rendre Admin");
            btnAdmin.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_lock_idle_lock, 0, 0, 0);
        }

        // 3. Mise à jour dynamique du Statut de compte
        if (user.isDisabled()) {
            btnDisable.setText("Réactiver le compte");
            btnDisable.setTextColor(getResources().getColor(R.color.primary));
            // Optionnel : changer l'icône pour réactiver
            btnDisable.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_input_add, 0, 0, 0);
        } else {
            btnDisable.setText("Désactiver le compte");
            btnDisable.setTextColor(getResources().getColor(R.color.error));
            btnDisable.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_delete, 0, 0, 0);
        }

        // 4. Création et affichage du dialogue
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Actions : " + user.getEmail())
                .setView(dialogView)
                .setNegativeButton("Fermer", null)
                .create();

        // 5. Click Listeners vers l'API
        btnAdmin.setOnClickListener(v -> {
            dialog.dismiss();
            String action = user.isAdmin() ? "Révoquer Admin" : "Rendre Admin";
            handleAction(action, user);
        });

        btnDisable.setOnClickListener(v -> {
            dialog.dismiss();
            String action = user.isDisabled() ? "Activer le compte" : "Désactiver le compte";
            handleAction(action, user);
        });

        dialog.show();
    }

    private void handleAction(String action, AdminUser user) {
        Call<AdminUser> call = null;

        switch (action) {
            case "Rendre Admin": call = adminApi.makeAdmin(user.getUid()); break;
            case "Révoquer Admin": call = adminApi.revokeAdmin(user.getUid()); break;
            case "Désactiver le compte": call = adminApi.disableUser(user.getUid()); break;
            case "Activer le compte": call = adminApi.enableUser(user.getUid()); break;
        }

        if (call != null) {
            setLoading(true);
            call.enqueue(new Callback<AdminUser>() {
                @Override
                public void onResponse(@NonNull Call<AdminUser> call, @NonNull Response<AdminUser> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(UsersManagementActivity.this, "Action réussie", Toast.LENGTH_SHORT).show();
                        loadUsers(); // Refresh list
                    } else {
                        setLoading(false);
                        Toast.makeText(UsersManagementActivity.this, "Action échouée", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AdminUser> call, @NonNull Throwable t) {
                    setLoading(false);
                    Toast.makeText(UsersManagementActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setLoading(boolean isLoading) {
        progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        swipeRefresh.setRefreshing(isLoading);
    }
}

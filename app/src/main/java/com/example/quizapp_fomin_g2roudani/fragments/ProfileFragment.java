package com.example.quizapp_fomin_g2roudani.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quizapp_fomin_g2roudani.MainActivity;
import com.example.quizapp_fomin_g2roudani.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvMemberSince;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();

        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        tvMemberSince = view.findViewById(R.id.tvProfileMemberSince);

        setupProfile();

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> 
            Toast.makeText(getActivity(), "Bientôt disponible", Toast.LENGTH_SHORT).show());

        return view;
    }

    private void setupProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = user.getEmail() != null ? user.getEmail().split("@")[0] : "Utilisateur";
            }
            tvName.setText(name);
            tvEmail.setText(user.getEmail());

            FirebaseUserMetadata metadata = user.getMetadata();
            if (metadata != null) {
                long creationTimestamp = metadata.getCreationTimestamp();
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.FRANCE);
                String dateStr = sdf.format(new Date(creationTimestamp));
                tvMemberSince.setText("Membre depuis " + dateStr);
            }
        }
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }
}

package com.example.quizapp_fomin_g2roudani;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.quizapp_fomin_g2roudani.fragments.HistoryFragment;
import com.example.quizapp_fomin_g2roudani.fragments.HomeFragment;
import com.example.quizapp_fomin_g2roudani.fragments.LeaderboardFragment;
import com.example.quizapp_fomin_g2roudani.fragments.ProfileFragment;
import com.example.quizapp_fomin_g2roudani.fragments.StatsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        // Gérer la redirection depuis d'autres activités (ex: Score)
        String target = getIntent().getStringExtra("target_fragment");
        if (target != null) {
            if (target.equals("leaderboard")) {
                loadFragment(new LeaderboardFragment());
                bottomNav.setSelectedItemId(R.id.nav_leaderboard);
            } else if (target.equals("history")) {
                loadFragment(new HistoryFragment());
                bottomNav.setSelectedItemId(R.id.nav_history);
            } else {
                loadDefaultFragment(savedInstanceState);
            }
        } else {
            loadDefaultFragment(savedInstanceState);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment = null;

            if (id == R.id.nav_home) fragment = new HomeFragment();
            else if (id == R.id.nav_stats) fragment = new StatsFragment();
            else if (id == R.id.nav_leaderboard) fragment = new LeaderboardFragment();
            else if (id == R.id.nav_history) fragment = new HistoryFragment();
            else if (id == R.id.nav_profile) fragment = new ProfileFragment();

            return loadFragment(fragment);
        });
    }

    private void loadDefaultFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}

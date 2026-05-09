package com.example.quizapp_fomin_g2roudani.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp_fomin_g2roudani.R;
import com.example.quizapp_fomin_g2roudani.models.LeaderboardEntry;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<LeaderboardEntry> items;

    public LeaderboardAdapter(List<LeaderboardEntry> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardEntry entry = items.get(position);
        
        // Calcul du rang et du nom anonyme côté client
        int rank = position + 1;
        String anonymousName = "Joueur #" + rank;

        holder.tvRank.setText("#" + rank);
        holder.tvUsername.setText(anonymousName);
        holder.tvScore.setText(entry.getScore() + " / " + entry.getTotal());

        // Style visuel pour le TOP 3
        applyTop3Style(holder.tvRank, rank);
    }

    private void applyTop3Style(TextView tvRank, int rank) {
        switch (rank) {
            case 1:
                tvRank.setTextColor(Color.parseColor("#FFD700")); // Or
                tvRank.setTextSize(22);
                break;
            case 2:
                tvRank.setTextColor(Color.parseColor("#C0C0C0")); // Argent
                tvRank.setTextSize(18);
                break;
            case 3:
                tvRank.setTextColor(Color.parseColor("#CD7F32")); // Bronze
                tvRank.setTextSize(18);
                break;
            default:
                tvRank.setTextColor(Color.parseColor("#64748B")); // Gris standard
                tvRank.setTextSize(16);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvUsername, tvScore;

        ViewHolder(View view) {
            super(view);
            tvRank = view.findViewById(R.id.tvRank);
            tvUsername = view.findViewById(R.id.tvUsername);
            tvScore = view.findViewById(R.id.tvScore);
        }
    }
}

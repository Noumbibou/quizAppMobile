package com.example.quizapp_fomin_g2roudani.adapters;

import android.content.res.ColorStateList;
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
        holder.tvRank.setText("#" + entry.getRank());
        holder.tvUsername.setText(entry.getUsername());
        holder.tvScore.setText(entry.getScore() + " / " + entry.getTotal());

        // Style spécial pour le TOP 3
        switch (entry.getRank()) {
            case 1:
                holder.tvRank.setTextColor(Color.parseColor("#FFD700")); // Or
                holder.tvRank.setTextSize(20);
                break;
            case 2:
                holder.tvRank.setTextColor(Color.parseColor("#C0C0C0")); // Argent
                break;
            case 3:
                holder.tvRank.setTextColor(Color.parseColor("#CD7F32")); // Bronze
                break;
            default:
                holder.tvRank.setTextColor(Color.parseColor("#64748B")); // Gris standard
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

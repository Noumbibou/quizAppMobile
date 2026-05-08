package com.example.quizapp_fomin_g2roudani.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp_fomin_g2roudani.R;
import com.example.quizapp_fomin_g2roudani.SelectLevel;
import com.example.quizapp_fomin_g2roudani.models.UserScore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MyScoresAdapter extends RecyclerView.Adapter<MyScoresAdapter.ViewHolder> {

    private final List<UserScore> items;

    public MyScoresAdapter(List<UserScore> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_score, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserScore item = items.get(position);
        holder.tvScoreLevel.setText("Niveau " + formatLevel(item.getLevel()));
        holder.tvScoreValue.setText(item.getScore() + " / " + item.getTotal());
        holder.tvScoreDate.setText(formatDate(item.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatLevel(String level) {
        if (level == null) return "-";
        switch (level) {
            case SelectLevel.LEVEL_BEGINNER: return "Débutant";
            case SelectLevel.LEVEL_INTERMEDIATE: return "Intermédiaire";
            case SelectLevel.LEVEL_ADVANCED: return "Avancé";
            default: return level;
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null) return "";
        try {
            // FastAPI usually returns ISO 8601 strings like "2023-10-12T14:30:00"
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(dateStr);
            
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy à HH:mm", Locale.FRANCE);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvScoreLevel, tvScoreDate, tvScoreValue;

        ViewHolder(View view) {
            super(view);
            tvScoreLevel = view.findViewById(R.id.tvScoreLevel);
            tvScoreDate = view.findViewById(R.id.tvScoreDate);
            tvScoreValue = view.findViewById(R.id.tvScoreValue);
        }
    }
}

package com.example.quizapp_fomin_g2roudani.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp_fomin_g2roudani.R;
import com.example.quizapp_fomin_g2roudani.models.QuestionSet;
import com.google.android.material.chip.Chip;

import java.util.List;

public class QuestionSetAdapter extends RecyclerView.Adapter<QuestionSetAdapter.ViewHolder> {

    private final List<QuestionSet> sets;
    private final OnSetClickListener listener;

    public interface OnSetClickListener {
        void onSetClick(QuestionSet set);
    }

    public QuestionSetAdapter(List<QuestionSet> sets, OnSetClickListener listener) {
        this.sets = sets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question_set, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuestionSet set = sets.get(position);
        holder.tvName.setText(set.getName());
        holder.chipLevel.setText(set.getLevel().toUpperCase());
        holder.tvCount.setText(set.getQuestionsCount() + " Q");

        if (set.isActive()) {
            holder.chipStatus.setText("ACTIF");
            holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_green_light);
        } else {
            holder.chipStatus.setText("INACTIF");
            holder.chipStatus.setChipBackgroundColorResource(android.R.color.darker_gray);
        }

        holder.itemView.setOnClickListener(v -> listener.onSetClick(set));
    }

    @Override
    public int getItemCount() {
        return sets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCount;
        Chip chipLevel, chipStatus;

        ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvSetName);
            tvCount = view.findViewById(R.id.tvQuestionsCount);
            chipLevel = view.findViewById(R.id.chipSetLevel);
            chipStatus = view.findViewById(R.id.chipSetStatus);
        }
    }
}

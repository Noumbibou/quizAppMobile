package com.example.quizapp_fomin_g2roudani.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp_fomin_g2roudani.R;
import com.example.quizapp_fomin_g2roudani.models.AdminQuestion;
import com.google.android.material.chip.Chip;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {

    private final List<AdminQuestion> questions;
    private final OnQuestionClickListener listener;

    public interface OnQuestionClickListener {
        void onQuestionClick(AdminQuestion question);
    }

    public QuestionAdapter(List<AdminQuestion> questions, OnQuestionClickListener listener) {
        this.questions = questions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminQuestion question = questions.get(position);
        holder.tvQuestionText.setText(question.getQuestion());
        holder.chipLevel.setText(question.getLevel().toUpperCase());
        
        if (question.isActive()) {
            holder.chipStatus.setText("ACTIF");
            holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_green_light);
        } else {
            holder.chipStatus.setText("DÉSACTIVÉ");
            holder.chipStatus.setChipBackgroundColorResource(android.R.color.darker_gray);
        }

        holder.tvCorrectOption.setText("Réponse : " + question.getCorrectOption());
        
        holder.itemView.setOnClickListener(v -> listener.onQuestionClick(question));
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionText, tvCorrectOption;
        Chip chipLevel, chipStatus;

        ViewHolder(View view) {
            super(view);
            tvQuestionText = view.findViewById(R.id.tvQuestionText);
            tvCorrectOption = view.findViewById(R.id.tvCorrectOption);
            chipLevel = view.findViewById(R.id.chipLevel);
            chipStatus = view.findViewById(R.id.chipStatus);
        }
    }
}

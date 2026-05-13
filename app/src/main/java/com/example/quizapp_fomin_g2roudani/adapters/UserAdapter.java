package com.example.quizapp_fomin_g2roudani.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp_fomin_g2roudani.R;
import com.example.quizapp_fomin_g2roudani.models.AdminUser;
import com.google.android.material.chip.Chip;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final List<AdminUser> users;
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(AdminUser user);
    }

    public UserAdapter(List<AdminUser> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminUser user = users.get(position);
        holder.tvEmail.setText(user.getEmail());
        holder.chipAdmin.setVisibility(user.isAdmin() ? View.VISIBLE : View.GONE);
        holder.chipDisabled.setVisibility(user.isDisabled() ? View.VISIBLE : View.GONE);
        
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail;
        Chip chipAdmin, chipDisabled;

        ViewHolder(View view) {
            super(view);
            tvEmail = view.findViewById(R.id.tvUserEmail);
            chipAdmin = view.findViewById(R.id.chipAdmin);
            chipDisabled = view.findViewById(R.id.chipDisabled);
        }
    }
}

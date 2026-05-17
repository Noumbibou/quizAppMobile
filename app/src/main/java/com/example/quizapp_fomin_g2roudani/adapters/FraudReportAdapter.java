package com.example.quizapp_fomin_g2roudani.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp_fomin_g2roudani.R;
import com.example.quizapp_fomin_g2roudani.models.FraudReport;

import java.util.List;

public class FraudReportAdapter extends RecyclerView.Adapter<FraudReportAdapter.ViewHolder> {

    private final List<FraudReport> reports;

    public FraudReportAdapter(List<FraudReport> reports) {
        this.reports = reports;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fraud_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FraudReport report = reports.get(position);
        
        holder.tvEmail.setText(report.getUserEmail());
        holder.tvDetails.setText("Niveau: " + report.getLevel() + " | Score: " + report.getScore() + "/" + report.getTotal());
        holder.tvCheatScore.setText("Indice suspicion : " + report.getCheatScore());
        holder.tvTimeSpent.setText("Temps : " + report.getTimeSpent() + "s");
        
        if (report.getLatitude() != null && report.getLongitude() != null) {
            holder.tvLocation.setText(String.format("GPS: %.4f, %.4f", report.getLatitude(), report.getLongitude()));
        } else {
            holder.tvLocation.setText("GPS: Non disponible");
        }

        holder.tvCameraStatus.setText("Caméra: " + (report.isCameraActive() ? "Active" : "Inactive"));
        holder.tvCameraStatus.setTextColor(report.isCameraActive() ? Color.parseColor("#2E7D32") : Color.RED);
        
        holder.tvDate.setText("Signalé le : " + report.getCreatedAt());
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvDetails, tvCheatScore, tvTimeSpent, tvLocation, tvCameraStatus, tvDate;

        ViewHolder(View view) {
            super(view);
            tvEmail = view.findViewById(R.id.tvFraudUserEmail);
            tvDetails = view.findViewById(R.id.tvFraudDetails);
            tvCheatScore = view.findViewById(R.id.tvCheatScore);
            tvTimeSpent = view.findViewById(R.id.tvTimeSpent);
            tvLocation = view.findViewById(R.id.tvLocation);
            tvCameraStatus = view.findViewById(R.id.tvCameraStatus);
            tvDate = view.findViewById(R.id.tvFraudDate);
        }
    }
}

package com.example.appquiz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquiz.R;
import com.example.appquiz.model.ScoreRecord;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

    private List<ScoreRecord> scoreList;

    public ScoreAdapter(List<ScoreRecord> scoreList) {
        this.scoreList = scoreList;
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        ScoreRecord record = scoreList.get(position);
        Context context = holder.itemView.getContext();

        holder.txtScoreValue.setText(String.format("Score : %d / %d", record.getScore(), record.getTotalQuestions()));
        holder.txtScoreDate.setText(record.getTimestamp());

        double percentage = record.getPercentage();
        holder.txtBadgePercentage.setText(String.format("%.0f%%", percentage));

        // Dynamic styling based on performance
        if (percentage >= 80) {
            holder.cardScoreBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.badge_good));
            holder.cardScoreBadge.setStrokeColor(ContextCompat.getColor(context, R.color.badge_good));
            holder.txtBadgePercentage.setTextColor(ContextCompat.getColor(context, R.color.text_white));
            
            holder.txtPerformanceLabel.setText("Excellent");
            holder.txtPerformanceLabel.setTextColor(ContextCompat.getColor(context, R.color.correct_text));
        } else if (percentage >= 60) {
            holder.cardScoreBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.badge_medium));
            holder.cardScoreBadge.setStrokeColor(ContextCompat.getColor(context, R.color.badge_medium));
            holder.txtBadgePercentage.setTextColor(ContextCompat.getColor(context, R.color.text_white));
            
            holder.txtPerformanceLabel.setText("Moyen");
            holder.txtPerformanceLabel.setTextColor(ContextCompat.getColor(context, R.color.badge_medium));
        } else {
            holder.cardScoreBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.badge_poor));
            holder.cardScoreBadge.setStrokeColor(ContextCompat.getColor(context, R.color.badge_poor));
            holder.txtBadgePercentage.setTextColor(ContextCompat.getColor(context, R.color.text_white));
            
            holder.txtPerformanceLabel.setText("À réviser");
            holder.txtPerformanceLabel.setTextColor(ContextCompat.getColor(context, R.color.incorrect_text));
        }
    }

    @Override
    public int getItemCount() {
        return scoreList.size();
    }

    public void updateList(List<ScoreRecord> newList) {
        this.scoreList = newList;
        notifyDataSetChanged();
    }

    static class ScoreViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardScoreBadge;
        TextView txtBadgePercentage;
        TextView txtScoreValue;
        TextView txtScoreDate;
        TextView txtPerformanceLabel;

        public ScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            cardScoreBadge = itemView.findViewById(R.id.cardScoreBadge);
            txtBadgePercentage = itemView.findViewById(R.id.txtBadgePercentage);
            txtScoreValue = itemView.findViewById(R.id.txtScoreValue);
            txtScoreDate = itemView.findViewById(R.id.txtScoreDate);
            txtPerformanceLabel = itemView.findViewById(R.id.txtPerformanceLabel);
        }
    }
}

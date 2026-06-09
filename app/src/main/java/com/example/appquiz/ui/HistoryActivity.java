package com.example.appquiz.ui;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appquiz.adapter.ScoreAdapter;
import com.example.appquiz.database.DatabaseHelper;
import com.example.appquiz.databinding.ActivityHistoryBinding;
import com.example.appquiz.model.ScoreRecord;
import com.example.appquiz.viewmodel.HistoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private HistoryViewModel viewModel;
    private ScoreAdapter scoreAdapter;
    private DatabaseHelper dbHelper;

    private static final String PREFS_NAME = "QuizPrefs";
    private static final String KEY_BEST_SCORE = "best_score";
    private static final String KEY_BEST_SCORE_TOTAL = "best_score_total";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize helpers
        dbHelper = new DatabaseHelper(this);
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        // Setup RecyclerView
        binding.recyclerViewScores.setLayoutManager(new LinearLayoutManager(this));
        scoreAdapter = new ScoreAdapter(new ArrayList<>());
        binding.recyclerViewScores.setAdapter(scoreAdapter);

        // Observe scores LiveData
        viewModel.getScores().observe(this, new Observer<List<ScoreRecord>>() {
            @Override
            public void onChanged(List<ScoreRecord> scores) {
                if (scores == null || scores.isEmpty()) {
                    showScores(new ArrayList<>());
                } else {
                    showScores(scores);
                }
            }
        });

        // Back button listener
        binding.btnBack.setOnClickListener(v -> finish());

        // Clear history button listener
        binding.btnClearHistory.setOnClickListener(v -> showClearConfirmationDialog());
    }

    private void showClearConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Effacer l'historique ?")
                .setMessage("Voulez-vous vraiment effacer tout l'historique des scores ainsi que votre meilleur score ? Cette action est irréversible.")
                .setPositiveButton("Effacer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear database
                        dbHelper.clearHistory();
                        // Clear SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove(KEY_BEST_SCORE);
                        editor.remove(KEY_BEST_SCORE_TOTAL);
                        editor.apply();
                        Toast.makeText(HistoryActivity.this, "Historique et meilleur score réinitialisés", Toast.LENGTH_SHORT).show();
                        showScores(new ArrayList<>());
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showScores(List<ScoreRecord> scores) {
        boolean hasScores = scores != null && !scores.isEmpty();
        binding.recyclerViewScores.setVisibility(hasScores ? View.VISIBLE : View.GONE);
        binding.layoutEmptyState.setVisibility(hasScores ? View.GONE : View.VISIBLE);
        binding.btnClearHistory.setVisibility(hasScores ? View.VISIBLE : View.GONE);
        scoreAdapter.updateList(hasScores ? scores : new ArrayList<>());
        updateSummary(hasScores ? scores : new ArrayList<>());
    }

    private void updateSummary(List<ScoreRecord> scores) {
        int totalSessions = scores.size();
        double averagePercentage = 0;

        for (ScoreRecord score : scores) {
            averagePercentage += score.getPercentage();
        }

        if (totalSessions > 0) {
            averagePercentage = averagePercentage / totalSessions;
        }

        binding.txtTotalSessions.setText(String.valueOf(totalSessions));
        binding.txtAverageScore.setText(String.format(java.util.Locale.getDefault(), "%.0f%%", averagePercentage));
    }
}

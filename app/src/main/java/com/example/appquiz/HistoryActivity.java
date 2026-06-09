package com.example.appquiz;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquiz.adapter.ScoreAdapter;
import com.example.appquiz.database.DatabaseHelper;
import com.example.appquiz.model.ScoreRecord;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewScores;
    private ScoreAdapter scoreAdapter;
    private List<ScoreRecord> scoreRecordList;
    private LinearLayout layoutEmptyState;
    private MaterialButton btnClearHistory;
    private View btnBack;

    private DatabaseHelper dbHelper;

    private static final String PREFS_NAME = "QuizPrefs";
    private static final String KEY_BEST_SCORE = "best_score";
    private static final String KEY_BEST_SCORE_TOTAL = "best_score_total";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Bind Views
        recyclerViewScores = findViewById(R.id.recyclerViewScores);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnClearHistory = findViewById(R.id.btnClearHistory);
        btnBack = findViewById(R.id.btnBack);

        dbHelper = new DatabaseHelper(this);

        // Setup RecyclerView
        recyclerViewScores.setLayoutManager(new LinearLayoutManager(this));
        scoreRecordList = new ArrayList<>();
        scoreAdapter = new ScoreAdapter(scoreRecordList);
        recyclerViewScores.setAdapter(scoreAdapter);

        // Load scores from SQLite
        loadScores();

        // Listeners
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearConfirmationDialog();
            }
        });
    }

    private void loadScores() {
        scoreRecordList = dbHelper.getAllScores();
        
        if (scoreRecordList.isEmpty()) {
            recyclerViewScores.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
            btnClearHistory.setVisibility(View.GONE);
        } else {
            recyclerViewScores.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            btnClearHistory.setVisibility(View.VISIBLE);
            scoreAdapter.updateList(scoreRecordList);
        }
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
                        loadScores();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}

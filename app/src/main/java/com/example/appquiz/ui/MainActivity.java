package com.example.appquiz.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.appquiz.R;
import com.example.appquiz.database.DatabaseHelper;
import com.example.appquiz.model.Question;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView txtProgress;
    private TextView txtCurrentScore;
    private TextView txtQuestion;
    private TextView bestScoreText;
    private TextView txtTimer;
    private LinearProgressIndicator progressIndicator;
    private RadioGroup optionsRadioGroup;
    private final RadioButton[] optionButtons = new RadioButton[4];
    private MaterialButton btnNext;
    private View btnGoToHistory;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean isAnswerChecked = false;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private CountDownTimer timer;
    private ToneGenerator toneGenerator;

    private static final String PREFS_NAME = "QuizPrefs";
    private static final String KEY_BEST_SCORE = "best_score";
    private static final String KEY_BEST_SCORE_TOTAL = "best_score_total";
    private static final long TIME_PER_QUESTION = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        bindViews();
        setupQuestions();
        setupTimer();
        updateBestScoreDisplay();
        loadQuestion(currentQuestionIndex);
        timer.start();

        optionsRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1 && !isAnswerChecked) {
                btnNext.setEnabled(true);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (!isAnswerChecked) {
                checkAnswer();
            } else {
                currentQuestionIndex++;
                if (currentQuestionIndex < questionList.size()) {
                    loadQuestion(currentQuestionIndex);
                    timer.cancel();
                    setupTimer();
                    timer.start();
                } else {
                    finishQuiz();
                }
            }
        });

        btnGoToHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    private void bindViews() {
        bestScoreText = findViewById(R.id.bestScoreText);
        txtProgress = findViewById(R.id.txtProgress);
        txtCurrentScore = findViewById(R.id.txtCurrentScore);
        txtQuestion = findViewById(R.id.txtQuestion);
        progressIndicator = findViewById(R.id.progressIndicator);
        optionsRadioGroup = findViewById(R.id.optionsRadioGroup);
        txtTimer = findViewById(R.id.txtTimer);
        optionButtons[0] = findViewById(R.id.option1);
        optionButtons[1] = findViewById(R.id.option2);
        optionButtons[2] = findViewById(R.id.option3);
        optionButtons[3] = findViewById(R.id.option4);
        btnNext = findViewById(R.id.btnNext);
        btnGoToHistory = findViewById(R.id.btnGoToHistory);
    }

    private void setupTimer() {
        timer = new CountDownTimer(TIME_PER_QUESTION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                txtTimer.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                txtTimer.setText("0");
                if (!isAnswerChecked) {
                    checkAnswer();
                }
            }
        };
    }

    private void setupQuestions() {
        questionList = new ArrayList<>();
        questionList.add(new Question(
                "Quel est le rôle principal de la méthode onCreate() dans une activité Android ?",
                new String[]{
                        "Démarrer un service en arrière-plan",
                        "Initialiser l'activité et charger son layout XML",
                        "Gérer la base de données SQLite",
                        "Demander les permissions de l'application"
                }, 1));

        questionList.add(new Question(
                "Quel langage est historiquement le langage officiel du développement Android ?",
                new String[]{"Python", "C++", "Java", "Swift"},
                2));

        questionList.add(new Question(
                "Quelle classe SQLite facilite la création et la mise à jour de bases de données ?",
                new String[]{"SQLiteOpenHelper", "SQLiteDatabase", "Cursor", "ContentValues"},
                0));

        questionList.add(new Question(
                "Quel composant affiche de longues listes de données de manière optimisée ?",
                new String[]{"ScrollView", "ListView", "RecyclerView", "LinearLayout"},
                2));

        questionList.add(new Question(
                "Quel mécanisme stocke de simples paires clé-valeur persistantes sous Android ?",
                new String[]{"La base de données SQLite", "SharedPreferences", "Les Intents", "Le Bundle"},
                1));
    }

    private void loadQuestion(int index) {
        Question question = questionList.get(index);
        txtQuestion.setText(question.getQuestionText());

        optionsRadioGroup.clearCheck();
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].setText(question.getOptions().get(i));
            optionButtons[i].setEnabled(true);
            optionButtons[i].setBackgroundResource(R.drawable.option_selector);
            optionButtons[i].setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }

        txtProgress.setText(getString(R.string.question_progress, index + 1, questionList.size()));
        int progress = (int) (((double) index / questionList.size()) * 100);
        progressIndicator.setProgress(progress);

        btnNext.setText(getString(R.string.btn_next));
        btnNext.setEnabled(false);
        isAnswerChecked = false;
    }

    private void checkAnswer() {
        if (timer != null) {
            timer.cancel();
        }

        isAnswerChecked = true;
        Question question = questionList.get(currentQuestionIndex);
        int correctIndex = question.getCorrectOptionIndex();
        int selectedIndex = getSelectedOptionIndex();

        if (selectedIndex == correctIndex) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK);
            score++;
            txtCurrentScore.setText(getString(R.string.current_score, score));
        } else {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK);
        }

        if (selectedIndex != -1 && selectedIndex != correctIndex) {
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            optionButtons[selectedIndex].startAnimation(shake);
        }

        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].setEnabled(false);
            if (i == correctIndex) {
                optionButtons[i].setBackgroundResource(R.drawable.option_correct);
                optionButtons[i].setTextColor(ContextCompat.getColor(this, R.color.correct_text));
            } else if (i == selectedIndex) {
                optionButtons[i].setBackgroundResource(R.drawable.option_incorrect);
                optionButtons[i].setTextColor(ContextCompat.getColor(this, R.color.incorrect_text));
            }
        }

        btnNext.setText(currentQuestionIndex == questionList.size() - 1
                ? getString(R.string.btn_finish)
                : getString(R.string.btn_next));
        btnNext.setEnabled(true);
    }

    private int getSelectedOptionIndex() {
        int selectedRadioButtonId = optionsRadioGroup.getCheckedRadioButtonId();
        for (int i = 0; i < optionButtons.length; i++) {
            if (optionButtons[i].getId() == selectedRadioButtonId) {
                return i;
            }
        }
        return -1;
    }

    private void finishQuiz() {
        progressIndicator.setProgress(100);

        int bestScore = sharedPreferences.getInt(KEY_BEST_SCORE, -1);
        boolean isNewRecord = score > bestScore;

        if (isNewRecord) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(KEY_BEST_SCORE, score);
            editor.putInt(KEY_BEST_SCORE_TOTAL, questionList.size());
            editor.apply();
            updateBestScoreDisplay();
        }

        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        dbHelper.addScore(score, questionList.size(), currentDate);

        String recordMsg = isNewRecord ? getString(R.string.new_record_yes) : getString(R.string.new_record_no);
        String dialogMessage = getString(R.string.quiz_completed_msg, score, questionList.size(), recordMsg);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.quiz_completed_title))
                .setMessage(dialogMessage)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.btn_history), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                        resetQuiz();
                    }
                })
                .setNegativeButton(getString(R.string.btn_restart), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetQuiz();
                    }
                })
                .setNeutralButton("Partager", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, dialogMessage);
                        startActivity(Intent.createChooser(shareIntent, "Partager le score"));
                    }
                })
                .show();
    }

    private void resetQuiz() {
        currentQuestionIndex = 0;
        score = 0;
        txtCurrentScore.setText(getString(R.string.current_score, score));
        loadQuestion(currentQuestionIndex);
        if (timer != null) {
            timer.cancel();
        }
        setupTimer();
        timer.start();
    }

    private void updateBestScoreDisplay() {
        int bestScore = sharedPreferences.getInt(KEY_BEST_SCORE, -1);
        int totalScore = sharedPreferences.getInt(KEY_BEST_SCORE_TOTAL, -1);

        if (bestScore != -1 && totalScore != -1) {
            bestScoreText.setText(getString(R.string.best_score_label, bestScore, totalScore));
        } else {
            bestScoreText.setText(getString(R.string.no_best_score));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bestScoreText != null) {
            updateBestScoreDisplay();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        if (toneGenerator != null) {
            toneGenerator.release();
        }
    }
}

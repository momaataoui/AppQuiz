package com.example.appquiz.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.appquiz.R;
import com.example.appquiz.database.DatabaseHelper;
import com.example.appquiz.model.Question;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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
    private TextView txtExplanation;
    private LinearProgressIndicator progressIndicator;
    private MaterialCardView timerCard;
    private MaterialCardView explanationCard;
    private final MaterialCardView[] optionCards = new MaterialCardView[4];
    private final TextView[] optionTexts = new TextView[4];
    private final TextView[] optionLetters = new TextView[4];
    private MaterialButton btnNext;
    private View btnGoToHistory;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean isAnswerChecked = false;
    private int selectedOptionIndex = -1;

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

        setupOptionClickListeners();

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
        txtTimer = findViewById(R.id.txtTimer);
        timerCard = findViewById(R.id.timerCard);
        explanationCard = findViewById(R.id.explanationCard);
        txtExplanation = findViewById(R.id.txtExplanation);
        optionCards[0] = findViewById(R.id.option1);
        optionCards[1] = findViewById(R.id.option2);
        optionCards[2] = findViewById(R.id.option3);
        optionCards[3] = findViewById(R.id.option4);
        for (int i = 0; i < optionCards.length; i++) {
            optionTexts[i] = optionCards[i].findViewById(R.id.txtOptionText);
            optionLetters[i] = optionCards[i].findViewById(R.id.txtOptionLetter);
        }
        btnNext = findViewById(R.id.btnNext);
        btnGoToHistory = findViewById(R.id.btnGoToHistory);
    }

    private void setupOptionClickListeners() {
        for (int i = 0; i < optionCards.length; i++) {
            final int optionIndex = i;
            optionCards[i].setOnClickListener(v -> {
                if (!isAnswerChecked) {
                    selectedOptionIndex = optionIndex;
                    updateSelectedOptionStyle();
                    btnNext.setEnabled(true);
                }
            });
        }
    }

    private void setupTimer() {
        timer = new CountDownTimer(TIME_PER_QUESTION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                txtTimer.setText(String.valueOf(secondsRemaining));
                int timerColor = secondsRemaining < 5
                        ? ContextCompat.getColor(MainActivity.this, R.color.timer_warning)
                        : ContextCompat.getColor(MainActivity.this, R.color.primary);
                txtTimer.setTextColor(timerColor);
                timerCard.setStrokeColor(timerColor);
            }

            @Override
            public void onFinish() {
                txtTimer.setText("0");
                txtTimer.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.timer_warning));
                timerCard.setStrokeColor(ContextCompat.getColor(MainActivity.this, R.color.timer_warning));
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
                }, 1,
                "onCreate() est appelee une seule fois lors de la creation initiale de l'activite. Elle sert generalement a connecter le layout XML, initialiser les vues et preparer les donnees."));

        questionList.add(new Question(
                "Quel langage est historiquement le langage officiel du développement Android ?",
                new String[]{"Python", "C++", "Java", "Swift"},
                2,
                "Java a ete le langage principal d'Android depuis les premieres versions du SDK. Kotlin est aujourd'hui aussi officiel, mais Java reste tres present dans les projets existants."));

        questionList.add(new Question(
                "Quelle classe SQLite facilite la création et la mise à jour de bases de données ?",
                new String[]{"SQLiteOpenHelper", "SQLiteDatabase", "Cursor", "ContentValues"},
                0,
                "SQLiteOpenHelper centralise la creation et les migrations via onCreate() et onUpgrade(). Cela evite de disperser la logique de structure de base de donnees dans l'application."));

        questionList.add(new Question(
                "Quel composant affiche de longues listes de données de manière optimisée ?",
                new String[]{"ScrollView", "ListView", "RecyclerView", "LinearLayout"},
                2,
                "RecyclerView recycle les vues visibles au lieu de creer une vue pour chaque element. C'est le composant recommande pour les listes longues ou dynamiques."));

        questionList.add(new Question(
                "Quel mécanisme stocke de simples paires clé-valeur persistantes sous Android ?",
                new String[]{"La base de données SQLite", "SharedPreferences", "Les Intents", "Le Bundle"},
                1,
                "SharedPreferences convient aux petites donnees simples comme un score, un theme ou un etat de preference. Pour des donnees structurees ou volumineuses, SQLite ou Room est plus adapte."));
    }

    private void loadQuestion(int index) {
        Question question = questionList.get(index);
        txtQuestion.setText(question.getQuestionText());

        selectedOptionIndex = -1;
        explanationCard.setVisibility(View.GONE);
        explanationCard.setAlpha(0f);
        explanationCard.setTranslationY(40f);
        txtTimer.setText("15");
        txtTimer.setTextColor(ContextCompat.getColor(this, R.color.primary));
        timerCard.setStrokeColor(ContextCompat.getColor(this, R.color.primary));

        for (int i = 0; i < optionCards.length; i++) {
            optionTexts[i].setText(question.getOptions().get(i));
            optionLetters[i].setText(String.valueOf((char) ('A' + i)));
            optionCards[i].setEnabled(true);
            optionCards[i].setClickable(true);
            applyOptionStyle(i, R.color.card_background, R.color.border_color, R.color.text_primary);
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
            optionCards[selectedIndex].startAnimation(shake);
        }

        for (int i = 0; i < optionCards.length; i++) {
            optionCards[i].setEnabled(false);
            optionCards[i].setClickable(false);
            if (i == correctIndex) {
                applyOptionStyle(i, R.color.correct_bg, R.color.correct_border, R.color.correct_text);
            } else if (i == selectedIndex) {
                applyOptionStyle(i, R.color.incorrect_bg, R.color.incorrect_border, R.color.incorrect_text);
            }
        }

        showExplanation(question.getExplanation());

        btnNext.setText(currentQuestionIndex == questionList.size() - 1
                ? getString(R.string.btn_finish)
                : getString(R.string.btn_next));
        btnNext.setEnabled(true);
    }

    private int getSelectedOptionIndex() {
        return selectedOptionIndex;
    }

    private void updateSelectedOptionStyle() {
        for (int i = 0; i < optionCards.length; i++) {
            if (i == selectedOptionIndex) {
                applyOptionStyle(i, R.color.selected_bg, R.color.selected_border, R.color.primary);
            } else {
                applyOptionStyle(i, R.color.card_background, R.color.border_color, R.color.text_primary);
            }
        }
    }

    private void applyOptionStyle(int index, int backgroundColorRes, int strokeColorRes, int textColorRes) {
        int backgroundColor = ContextCompat.getColor(this, backgroundColorRes);
        int strokeColor = ContextCompat.getColor(this, strokeColorRes);
        int textColor = ContextCompat.getColor(this, textColorRes);
        optionCards[index].setCardBackgroundColor(backgroundColor);
        optionCards[index].setStrokeColor(strokeColor);
        optionTexts[index].setTextColor(textColor);
        optionLetters[index].setTextColor(textColor);
    }

    private void showExplanation(String explanation) {
        txtExplanation.setText(explanation);
        explanationCard.setVisibility(View.VISIBLE);
        explanationCard.setAlpha(0f);
        explanationCard.setTranslationY(40f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(explanationCard, View.ALPHA, 0f, 1f);
        ObjectAnimator slideUp = ObjectAnimator.ofFloat(explanationCard, View.TRANSLATION_Y, 40f, 0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fadeIn, slideUp);
        animatorSet.setDuration(350);
        animatorSet.start();
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

        // Inflate custom dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quiz_result, null);

        // Bind views
        TextView txtResultEmoji = dialogView.findViewById(R.id.txtResultEmoji);
        TextView txtResultTitle = dialogView.findViewById(R.id.txtResultTitle);
        TextView txtResultScore = dialogView.findViewById(R.id.txtResultScore);
        TextView txtRecordBadge = dialogView.findViewById(R.id.txtRecordBadge);
        TextView txtResultSubtitle = dialogView.findViewById(R.id.txtResultSubtitle);
        LinearLayout starRow = dialogView.findViewById(R.id.starRow);
        MaterialButton btnHistory = dialogView.findViewById(R.id.btnDialogHistory);
        MaterialButton btnRestart = dialogView.findViewById(R.id.btnDialogRestart);
        MaterialButton btnShare = dialogView.findViewById(R.id.btnDialogShare);

        // Set emoji + title based on score
        String[] emojis = {"", "", "", "", "⭐", ""};
        String[] titles = {"Continue !", "Continue !", "Continue !", "Bien joué !", "Excellent !", "Parfait !"};
        txtResultEmoji.setText(emojis[score]);
        txtResultTitle.setText(titles[score]);
        txtResultScore.setText(score + " / " + questionList.size());
        txtResultSubtitle.setText("Votre score final est de " + score + " sur " + questionList.size());

        // Stars
        starRow.removeAllViews();
        for (int i = 0; i < questionList.size(); i++) {
            TextView star = new TextView(this);
            star.setText(i < score ? "⭐" : "☆");
            star.setTextSize(22);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(4, 0, 4, 0);
            star.setLayoutParams(lp);
            starRow.addView(star);
        }

        // Record badge
        if (isNewRecord) {
            txtRecordBadge.setVisibility(View.VISIBLE);
        } else {
            txtRecordBadge.setVisibility(View.GONE);
        }

        // Build and show dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnHistory.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            resetQuiz();
        });
        btnRestart.setOnClickListener(v -> {
            dialog.dismiss();
            resetQuiz();
        });
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "J'ai obtenu " + score + "/" + questionList.size() + " au Quiz Éducatif Android ! ");
            startActivity(Intent.createChooser(shareIntent, "Partager le score"));
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
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

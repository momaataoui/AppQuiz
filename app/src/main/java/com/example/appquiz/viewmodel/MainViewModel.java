package com.example.appquiz.viewmodel;

import android.app.Application;
import android.media.AudioManager;
import android.media.ToneGenerator;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.appquiz.model.Question;
import com.example.appquiz.repository.QuizRepository;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * ViewModel that holds the quiz state, timer and sound handling.
 */
public class MainViewModel extends AndroidViewModel {
    private final QuizRepository repository;
    private final MutableLiveData<List<Question>> questionList = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private final MutableLiveData<Long> timerSeconds = new MutableLiveData<>(15L);

    private Timer timer;
    private ToneGenerator toneGenerator;
    public static final long TIME_PER_QUESTION_MS = 15000L; // 15 seconds

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = QuizRepository.getInstance(application);
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        loadQuestions();
        startTimer();
    }

    private void loadQuestions() {
        // Repository already contains static questions
        questionList.setValue(repository.getQuestions().getValue());
    }

    public LiveData<List<Question>> getQuestionList() { return questionList; }
    public LiveData<Integer> getCurrentIndex() { return currentIndex; }
    public LiveData<Integer> getScore() { return score; }
    public LiveData<Long> getTimerSeconds() { return timerSeconds; }

    /** Called when user selects an answer */
    public void checkAnswer(int selectedIndex) {
        Question q = questionList.getValue().get(currentIndex.getValue());
        boolean correct = selectedIndex == q.getCorrectOptionIndex();
        if (correct) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK);
            score.setValue(score.getValue() + 1);
        } else {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK);
        }
        // Stop timer for this question
        if (timer != null) timer.cancel();
    }

    /** Move to next question; returns false if quiz is finished */
    public boolean nextQuestion() {
        int idx = currentIndex.getValue();
        idx++;
        if (idx < questionList.getValue().size()) {
            currentIndex.setValue(idx);
            resetTimer();
            return true;
        }
        // Quiz finished
        return false;
    }

    /** Save final score to DB via repository */
    public void persistScore(int finalScore) {
        int total = questionList.getValue().size();
        String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                .format(new java.util.Date());
        repository.insertScore(finalScore, total, date);
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            long remaining = TIME_PER_QUESTION_MS / 1000;
            @Override
            public void run() {
                if (remaining >= 0) {
                    timerSeconds.postValue(remaining);
                    remaining--;
                } else {
                    timer.cancel();
                    // Auto‑fail if time runs out (treated as incorrect answer)
                    checkAnswer(-1);
                }
            }
        }, 0, 1000);
    }

    private void resetTimer() {
        if (timer != null) timer.cancel();
        timerSeconds.setValue(15L);
        startTimer();
    }
}

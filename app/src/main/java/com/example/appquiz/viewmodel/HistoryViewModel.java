package com.example.appquiz.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.appquiz.model.ScoreRecord;
import com.example.appquiz.repository.QuizRepository;
import java.util.List;

/**
 * ViewModel for HistoryActivity. Exposes the list of ScoreRecord objects stored in the repository.
 */
public class HistoryViewModel extends AndroidViewModel {
    private final QuizRepository repository;
    private final LiveData<List<ScoreRecord>> scoresLiveData;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repository = QuizRepository.getInstance(application);
        scoresLiveData = repository.getAllScores();
    }

    public LiveData<List<ScoreRecord>> getScores() {
        return scoresLiveData;
    }
}

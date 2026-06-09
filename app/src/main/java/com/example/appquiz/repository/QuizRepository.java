package com.example.appquiz.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.appquiz.database.DatabaseHelper;
import com.example.appquiz.model.Question;
import com.example.appquiz.model.ScoreRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * Central data source for the app. Provides quiz questions and persists score records.
 * In a real‑world app this could combine remote API calls, caching, etc.
 */
public class QuizRepository {
    private static QuizRepository instance;
    private final DatabaseHelper dbHelper;
    private final MutableLiveData<List<Question>> questionsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ScoreRecord>> scoresLiveData = new MutableLiveData<>();

    private QuizRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
        loadQuestions();
        loadScores();
    }

    public static synchronized QuizRepository getInstance(Context context) {
        if (instance == null) {
            instance = new QuizRepository(context.getApplicationContext());
        }
        return instance;
    }

    /** Load static questions (could be moved to a JSON asset later) */
    private void loadQuestions() {
        List<Question> list = new ArrayList<>();
        list.add(new Question(
                "Quel est le rôle principal de la méthode onCreate() dans une activité Android ?",
                new String[]{"Démarrer un service en arrière-plan", "Initialiser l'activité et charger son layout XML", "Gérer la base de données SQLite", "Demander les permissions de l'application"},
                1));
        list.add(new Question(
                "Quel langage est historiquement le langage officiel du développement Android ?",
                new String[]{"Python", "C++", "Java", "Swift"},
                2));
        list.add(new Question(
                "Quelle classe SQLite facilite la création et la mise à jour de bases de données ?",
                new String[]{"SQLiteOpenHelper", "SQLiteDatabase", "Cursor", "ContentValues"},
                0));
        list.add(new Question(
                "Quel composant affiche de longues listes de données de manière optimisée ?",
                new String[]{"ScrollView", "ListView", "RecyclerView", "LinearLayout"},
                2));
        list.add(new Question(
                "Quel mécanisme stocke de simples paires clé‑valeur persistantes sous Android ?",
                new String[]{"La base de données SQLite", "SharedPreferences", "Les Intents", "Le Bundle"},
                1));
        questionsLiveData.setValue(list);
    }

    public LiveData<List<Question>> getQuestions() {
        return questionsLiveData;
    }

    /** Insert a new score record into SQLite */
    public void insertScore(int score, int total, String date) {
        dbHelper.addScore(score, total, date);
        // Reload scores so observers get the updated list
        loadScores();
    }

    private void loadScores() {
        List<ScoreRecord> list = dbHelper.getAllScores();
        scoresLiveData.setValue(list);
    }

    public LiveData<List<ScoreRecord>> getAllScores() {
        return scoresLiveData;
    }
}

package com.example.appquiz.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.appquiz.model.ScoreRecord;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AppQuiz.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_SCORES = "scores";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_SCORE = "score";
    public static final String COLUMN_TOTAL = "total_questions";
    public static final String COLUMN_DATE = "date_created";

    private static final String CREATE_TABLE_SCORES = "CREATE TABLE " + TABLE_SCORES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_SCORE + " INTEGER, " +
            COLUMN_TOTAL + " INTEGER, " +
            COLUMN_DATE + " TEXT" +
            ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SCORES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
        onCreate(db);
    }

    // Add score to history
    public boolean addScore(int score, int total, String dateCreated) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCORE, score);
        values.put(COLUMN_TOTAL, total);
        values.put(COLUMN_DATE, dateCreated);

        long result = db.insert(TABLE_SCORES, null, values);
        return result != -1;
    }

    // Retrieve all scores (latest first)
    public List<ScoreRecord> getAllScores() {
        List<ScoreRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SCORES + " ORDER BY " + COLUMN_ID + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                int score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE));
                int total = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));

                list.add(new ScoreRecord(id, score, total, date));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // Clear history
    public void clearHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SCORES, null, null);
        db.close();
    }
}

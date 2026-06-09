package com.example.appquiz.model;

import java.util.Arrays;
import java.util.List;

public class Question {
    private String questionText;
    private List<String> options;
    private int correctOptionIndex;

    public Question(String questionText, List<String> options, int correctOptionIndex) {
        this.questionText = questionText;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
    }

    public Question(String questionText, String[] optionsArray, int correctOptionIndex) {
        this.questionText = questionText;
        this.options = Arrays.asList(optionsArray);
        this.correctOptionIndex = correctOptionIndex;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }
}

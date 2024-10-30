package com.example.dell.myapplication;

public class Question {
    private final String chinese;
    private final String english;
    private boolean answered;

    public Question(String chinese, String english) {
        this.chinese = chinese;
        this.english = english;
    }

    public String getChinese() {
        return chinese;
    }

    public String getEnglish() {
        return english;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }
    public boolean isAnswered() {
        return answered;
    }

}

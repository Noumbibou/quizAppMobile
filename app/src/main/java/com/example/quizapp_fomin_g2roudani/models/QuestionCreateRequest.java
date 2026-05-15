package com.example.quizapp_fomin_g2roudani.models;

import com.google.gson.annotations.SerializedName;

public class QuestionCreateRequest {
    @SerializedName("question")
    private String question;

    @SerializedName("optionA")
    private String optionA;

    @SerializedName("optionB")
    private String optionB;

    @SerializedName("optionC")
    private String optionC;

    @SerializedName("optionD")
    private String optionD;

    @SerializedName("correct")
    private String correct;

    @SerializedName("level")
    private String level;

    @SerializedName("set_id")
    private int setId;

    public QuestionCreateRequest(String question, String optionA, String optionB, String optionC, String optionD, String correct, String level, int setId) {
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correct = correct;
        this.level = level;
        this.setId = setId;
    }

    // Getters and Setters
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public String getCorrect() { return correct; }
    public void setCorrect(String correct) { this.correct = correct; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getSetId() { return setId; }
    public void setSetId(int setId) { this.setId = setId; }
}

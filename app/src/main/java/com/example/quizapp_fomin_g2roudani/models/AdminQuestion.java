package com.example.quizapp_fomin_g2roudani.models;

import com.google.gson.annotations.SerializedName;

public class AdminQuestion {
    @SerializedName("id")
    private int id;

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
    private String correctOption; // Mappé sur "correct" du backend

    @SerializedName("level")
    private String level;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("set_id")
    private int setId;

    public AdminQuestion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
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
    public String getCorrectOption() { return correctOption; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public int getSetId() { return setId; }
    public void setSetId(int setId) { this.setId = setId; }
}

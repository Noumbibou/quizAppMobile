package com.example.quizapp_fomin_g2roudani.models;

import com.google.gson.annotations.SerializedName;

public class AdminQuestion {
    @SerializedName("id")
    private int id;

    @SerializedName("question")
    private String question;

    @SerializedName("option_a")
    private String optionA;

    @SerializedName("option_b")
    private String optionB;

    @SerializedName("option_c")
    private String optionC;

    @SerializedName("option_d")
    private String optionD;

    @SerializedName("correct_option")
    private String correctOption; // A, B, C, or D

    @SerializedName("level")
    private String level; // beginner, intermediate, advanced

    @SerializedName("is_active")
    private boolean isActive;

    public AdminQuestion() {}

    // Getters and Setters
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
}

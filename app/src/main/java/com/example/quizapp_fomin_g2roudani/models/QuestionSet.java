package com.example.quizapp_fomin_g2roudani.models;

import com.google.gson.annotations.SerializedName;

public class QuestionSet {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("level")
    private String level;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("questions_count")
    private int questionsCount;

    public QuestionSet() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getQuestionsCount() { return questionsCount; }
    public void setQuestionsCount(int questionsCount) { this.questionsCount = questionsCount; }
}

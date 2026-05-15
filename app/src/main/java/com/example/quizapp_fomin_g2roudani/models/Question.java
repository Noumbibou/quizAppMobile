package com.example.quizapp_fomin_g2roudani.models;

import com.google.gson.annotations.SerializedName;

public class Question {
    @SerializedName("id")
    private int id;
    
    @SerializedName("question")
    private String question;
    
    @SerializedName("optionA") // Changé de option_a à optionA
    private String optionA;
    
    @SerializedName("optionB") // Changé de option_b à optionB
    private String optionB;
    
    @SerializedName("optionC") // Changé de option_c à optionC
    private String optionC;
    
    @SerializedName("optionD") // Changé de option_d à optionD
    private String optionD;

    public Question() {}

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
}

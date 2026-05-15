package com.example.quizapp_fomin_g2roudani.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ImportResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("total_rows")
    private int totalRows;

    @SerializedName("imported")
    private int imported;

    @SerializedName("failed")
    private int failed;

    @SerializedName("errors")
    private List<ImportError> errors;

    public boolean isSuccess() { return success; }
    public int getTotalRows() { return totalRows; }
    public int getImported() { return imported; }
    public int getFailed() { return failed; }
    public List<ImportError> getErrors() { return errors; }

    public static class ImportError {
        @SerializedName("row")
        private int row;

        @SerializedName("message")
        private String message;

        public int getRow() { return row; }
        public String getMessage() { return message; }
    }
}

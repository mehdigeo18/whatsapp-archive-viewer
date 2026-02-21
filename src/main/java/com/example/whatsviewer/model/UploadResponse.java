package com.example.whatsviewer.model;

public class UploadResponse {

    private String message;
    private int totalMessages;

    public UploadResponse() {}

    public UploadResponse(String message, int totalMessages) {
        this.message = message;
        this.totalMessages = totalMessages;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getTotalMessages() { return totalMessages; }
    public void setTotalMessages(int totalMessages) { this.totalMessages = totalMessages; }
}

package com.example.whatsviewer.model;

import java.util.List;

public class UploadResponse {

    private String message;
    private int totalMessages;
    private List<ChatMessage> messages;

    public UploadResponse() {}

    public UploadResponse(String message, int totalMessages, List<ChatMessage> messages) {
        this.message = message;
        this.totalMessages = totalMessages;
        this.messages = messages;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getTotalMessages() { return totalMessages; }
    public void setTotalMessages(int totalMessages) { this.totalMessages = totalMessages; }

    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
}

package com.example.whatsviewer.model;

import java.util.List;

public class UploadResponse {
    private String chatName;
    private String myName;
    private int totalMessages;
    private List<ChatMessage> messages;

    // NEW: used to serve media from the uploaded ZIP
    private String uploadId;

    public UploadResponse() {}

    public UploadResponse(String chatName, String myName, List<ChatMessage> messages) {
        this.chatName = chatName;
        this.myName = myName;
        this.messages = messages;
        this.totalMessages = (messages == null) ? 0 : messages.size();
    }

    public String getChatName() { return chatName; }
    public void setChatName(String chatName) { this.chatName = chatName; }

    public String getMyName() { return myName; }
    public void setMyName(String myName) { this.myName = myName; }

    public int getTotalMessages() { return totalMessages; }
    public void setTotalMessages(int totalMessages) { this.totalMessages = totalMessages; }

    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        this.totalMessages = (messages == null) ? 0 : messages.size();
    }

    public String getUploadId() { return uploadId; }
    public void setUploadId(String uploadId) { this.uploadId = uploadId; }
}

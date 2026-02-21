package com.example.whatsviewer.model;

import java.util.List;

public class UploadResponse {

    private String chatName;
    private String myName;
    private int totalMessages;
    private List<ChatMessage> messages;

    // IMPORTANT: used for /api/media/{uploadId}?name=...
    private String uploadId;

    public UploadResponse() {}

    public String getChatName() { return chatName; }
    public void setChatName(String chatName) { this.chatName = chatName; }

    public String getMyName() { return myName; }
    public void setMyName(String myName) { this.myName = myName; }

    public int getTotalMessages() { return totalMessages; }
    public void setTotalMessages(int totalMessages) { this.totalMessages = totalMessages; }

    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }

    public String getUploadId() { return uploadId; }
    public void setUploadId(String uploadId) { this.uploadId = uploadId; }
}

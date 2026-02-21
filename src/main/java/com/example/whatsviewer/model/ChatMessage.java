package com.example.whatsviewer.model;

import java.time.LocalDateTime;

public class ChatMessage {

    private LocalDateTime timestamp;
    private String sender;
    private String content;

    public ChatMessage() {}

    public ChatMessage(LocalDateTime timestamp, String sender, String content) {
        this.timestamp = timestamp;
        this.sender = sender;
        this.content = content;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

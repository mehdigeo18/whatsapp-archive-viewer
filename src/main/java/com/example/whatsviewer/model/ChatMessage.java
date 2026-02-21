package com.example.whatsviewer.model;

public class ChatMessage {
    private String timestamp;
    private String sender;
    private String text;
    private boolean outgoing;
    private String attachmentName;

    public ChatMessage() {}

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isOutgoing() { return outgoing; }
    public void setOutgoing(boolean outgoing) { this.outgoing = outgoing; }

    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }
}

package com.example.whatsviewer.model;

import java.time.LocalDateTime;

public class ChatMessage {

  private LocalDateTime timestamp;
  private String sender;
  private boolean outgoing;

  // message body (text) - can be empty when it's an attachment-only message
  private String text;

  // If WhatsApp export contains: <attached: filename>
  private String attachmentName;

  // "image" | "audio" | "file" (best-effort)
  private String attachmentType;

  public ChatMessage() {}

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public boolean isOutgoing() {
    return outgoing;
  }

  public void setOutgoing(boolean outgoing) {
    this.outgoing = outgoing;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getAttachmentName() {
    return attachmentName;
  }

  public void setAttachmentName(String attachmentName) {
    this.attachmentName = attachmentName;
  }

  public String getAttachmentType() {
    return attachmentType;
  }

  public void setAttachmentType(String attachmentType) {
    this.attachmentType = attachmentType;
  }
}

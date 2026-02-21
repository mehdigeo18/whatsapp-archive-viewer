package com.example.whatsviewer.service;

import com.example.whatsviewer.model.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArchiveService {

    private final WhatsAppParser parser = new WhatsAppParser();

    public List<ChatMessage> processChat(String text) {
        return parser.parse(text);
    }
}

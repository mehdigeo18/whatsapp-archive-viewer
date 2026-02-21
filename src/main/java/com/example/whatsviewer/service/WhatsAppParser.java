package com.example.whatsviewer.service;

import com.example.whatsviewer.model.ChatMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhatsAppParser {

    private static final Pattern LINE_PATTERN =
            Pattern.compile("^\\[(\\d{2})/(\\d{2})/(\\d{4}), (\\d{2}):(\\d{2})] (.*?): (.*)$");

    public List<ChatMessage> parse(String text) {
        List<ChatMessage> messages = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");

        for (String line : lines) {
            Matcher matcher = LINE_PATTERN.matcher(line);
            if (matcher.matches()) {
                int day = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int year = Integer.parseInt(matcher.group(3));
                int hour = Integer.parseInt(matcher.group(4));
                int minute = Integer.parseInt(matcher.group(5));
                String sender = matcher.group(6);
                String content = matcher.group(7);

                LocalDateTime timestamp = LocalDateTime.of(year, month, day, hour, minute);
                messages.add(new ChatMessage(timestamp, sender, content));
            }
        }
        return messages;
    }
}

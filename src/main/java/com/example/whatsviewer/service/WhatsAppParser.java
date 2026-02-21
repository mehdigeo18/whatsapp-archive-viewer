package com.example.whatsviewer.service;

import com.example.whatsviewer.model.ChatMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Robust parser for common WhatsApp TXT exports.
 *
 * Supports lines like:
 *   [30/08/2023, 7:17:12 PM] Ali Mehdi: Hi
 *   ‎[30/08/2023, 7:17:12 PM] Ali Mehdi: Hi   (leading LTR mark)
 *   [30/08/2023, 7:17 PM] Ali Mehdi: Hi
 * And attachment markers:
 *   <attached: 00000003-PHOTO-...jpg>
 *
 * Multi-line messages are supported: any non-header line after a header is appended
 * to the current message until the next header appears.
 */
@Component
public class WhatsAppParser {

    // Strip common Unicode direction marks/BOM that appear in WhatsApp exports
    private static final Pattern LEADING_MARKS = Pattern.compile("^[\\u200e\\u200f\\u202a-\\u202e\\ufeff]+");

    // Header with optional seconds and inline message text
    private static final Pattern HEADER = Pattern.compile(
            "^\\[(\\d{1,2})/(\\d{1,2})/(\\d{2,4}),\\s*([0-9]{1,2}:[0-9]{2}(?::[0-9]{2})?)\\s*([AaPp][Mm])\\]\\s*(.*?):\\s*(.*)$"
    );

    private static final Pattern ATTACH = Pattern.compile("^<attached:\\s*(.+?)>\\s*$", Pattern.CASE_INSENSITIVE);

    public List<ChatMessage> parse(String rawText) {
        if (rawText == null) return List.of();

        String normalized = rawText.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = normalized.split("\n", -1);

        List<ChatMessage> out = new ArrayList<>();

        ChatMessage current = null;
        StringBuilder currentBody = null;

        for (String rawLine : lines) {
            String line = (rawLine == null) ? "" : rawLine.trim();

            // keep intentional blank lines inside a message
            if (line.isEmpty()) {
                if (currentBody != null) currentBody.append("\n");
                continue;
            }

            // remove leading unicode marks
            line = LEADING_MARKS.matcher(line).replaceFirst("");

            Matcher hm = HEADER.matcher(line);
            if (hm.matches()) {
                // flush previous
                if (current != null) {
                    current.setMessage(currentBody == null ? "" : currentBody.toString().trim());
                    out.add(current);
                }

                int dd = Integer.parseInt(hm.group(1));
                int mm = Integer.parseInt(hm.group(2));
                String yyyy = hm.group(3);
                String time = hm.group(4);
                String ampm = hm.group(5).toUpperCase(Locale.ROOT);
                String sender = hm.group(6).trim();
                String inlineText = hm.group(7);

                LocalDateTime ts = parseDate(dd, mm, yyyy, time, ampm);

                current = new ChatMessage();
                current.setTimestamp(ts.toString());
                current.setSender(sender);
                currentBody = new StringBuilder();
                if (inlineText != null && !inlineText.isBlank()) {
                    currentBody.append(inlineText.trim());
                }
                continue;
            }

            // attachment marker line
            Matcher am = ATTACH.matcher(line);
            if (am.matches() && currentBody != null) {
                if (currentBody.length() > 0 && currentBody.charAt(currentBody.length() - 1) != '\n') {
                    currentBody.append("\n");
                }
                currentBody.append("<attached: ").append(am.group(1).trim()).append(">");
                continue;
            }

            // continuation line (if we have a current message)
            if (currentBody != null) {
                if (currentBody.length() > 0 && currentBody.charAt(currentBody.length() - 1) != '\n') {
                    currentBody.append("\n");
                }
                currentBody.append(line);
            }
        }

        // flush last
        if (current != null) {
            current.setMessage(currentBody == null ? "" : currentBody.toString().trim());
            out.add(current);
        }

        return out;
    }

    private LocalDateTime parseDate(int dd, int mm, String yyyy, String timeStr, String ampm) {
        int year = (yyyy.length() == 2) ? (Integer.parseInt(yyyy) + 2000) : Integer.parseInt(yyyy);

        int hour;
        int minute;
        int second = 0;

        String[] parts = timeStr.split(":");
        hour = Integer.parseInt(parts[0]);
        minute = Integer.parseInt(parts[1]);
        if (parts.length > 2) {
            second = Integer.parseInt(parts[2]);
        }

        if ("PM".equals(ampm) && hour < 12) hour += 12;
        if ("AM".equals(ampm) && hour == 12) hour = 0;

        return LocalDateTime.of(year, mm, dd, hour, minute, second);
    }
}

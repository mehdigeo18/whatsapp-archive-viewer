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

@Component
public class WhatsAppParser {

    /**
     * WhatsApp exports sometimes include invisible bidi marks (e.g. \u200e) and narrow no‑break spaces (\u202f),
     * so we normalize each line before applying regex.
     *
     * Typical export formats:
     * [30/08/2023, 7:17:07 PM] Muzamil Car: Hi
     * [30/08/2023, 7:17:12 PM] Ali Mehdi: <attached: 00000003-PHOTO-...jpg>
     * Multi-line messages continue on subsequent lines until the next header line.
     */
   // Example WhatsApp line formats:
// 30/08/2023, 7:17 PM - Name: Hi
// 30/08/2023, 19:17 - Name: Hi

private static final Pattern HEADER_PATTERN = Pattern.compile(
    "^(\\d{1,2}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4}),?\\s+" +   // date
    "(\\d{1,2}:\\d{2})(?:\\s*(AM|PM))?\\s+-\\s+" +          // time (+ optional AM/PM)
    "([^:]+):\\s*(.*)$"                                     // sender: message
);

    private static final Pattern ATTACH_PATTERN = Pattern.compile(
            "^<attached:\s*(.+?)>\s*$",
            Pattern.CASE_INSENSITIVE
    );

    public List<ChatMessage> parse(String fileContent) {
        List<ChatMessage> messages = new ArrayList<>();
        if (fileContent == null || fileContent.isBlank()) return messages;

        String[] rawLines = fileContent.replace("\r\n", "\n").split("\n");

        ChatMessage current = null;
        StringBuilder body = new StringBuilder();

        for (String raw : rawLines) {
            // keep empty lines as part of body
            if (raw == null) continue;

            String line = normalizeLine(raw);

            Matcher hm = HEADER_PATTERN.matcher(line);
            if (hm.matches()) {
                // flush previous
                if (current != null) {
                    finalizeMessage(current, body, messages);
                }

                LocalDateTime dt = parseDateTime(
                        hm.group(1), hm.group(2), hm.group(3),
                        hm.group(4), hm.group(5)
                );
                String sender = hm.group(6).trim();
                String firstBody = hm.group(7) == null ? "" : hm.group(7).trim();

                current = new ChatMessage();
                current.setTimestamp(dateTime.toString());
                current.setSender(sender);
                current.setOutgoing(false); // set later in service when comparing names (optional)

                body.setLength(0);

                // handle inline body on same header line
                if (!firstBody.isEmpty()) {
                    Matcher am = ATTACH_PATTERN.matcher(firstBody);
                    if (am.matches()) {
                        current.setAttachmentName(am.group(1).trim());
                    } else {
                        body.append(firstBody);
                    }
                }
                continue;
            }

            // continuation line (only if we already have a current message)
            if (current == null) continue;

            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                // preserve blank line separation
                if (body.length() > 0) body.append("\n");
                continue;
            }

            // attachment on its own line
            Matcher am = ATTACH_PATTERN.matcher(trimmed);
            if (am.matches()) {
                current.setAttachmentName(am.group(1).trim());
            } else {
                if (body.length() > 0) body.append("\n");
                body.append(line);
            }
        }

        // flush last
        if (current != null) {
            finalizeMessage(current, body, messages);
        }

        return messages;
    }

    private static void finalizeMessage(ChatMessage msg, StringBuilder body, List<ChatMessage> out) {
        String txt = body.toString().trim();
        msg.setText(txt.isEmpty() ? null : txt);
        out.add(msg);
    }

    private static String normalizeLine(String raw) {
        // Remove bidi marks that often appear in WhatsApp exports
        String s = raw.replace("\u200e", "")
                .replace("\u200f", "")
                .replace("\ufeff", "");
        // Replace narrow no-break space with normal space so AM/PM matches reliably
        s = s.replace("\u202f", " ");
        return s;
    }

    private static LocalDateTime parseDateTime(String dd, String mm, String yy, String time, String ampm) {
        int day = Integer.parseInt(dd);
        int month = Integer.parseInt(mm);
        int year = yy.length() == 2 ? 2000 + Integer.parseInt(yy) : Integer.parseInt(yy);

        // time could be H:mm or H:mm:ss
        String normalizedTime = time;
        if (time.chars().filter(ch -> ch == ':').count() == 1) {
            normalizedTime = time + ":00";
        }

        String stamp = String.format(Locale.US, "%02d/%02d/%04d %s %s", day, month, year, normalizedTime, ampm.toUpperCase(Locale.ROOT));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm:ss a", Locale.US);
        return LocalDateTime.parse(stamp, fmt);
    }
}

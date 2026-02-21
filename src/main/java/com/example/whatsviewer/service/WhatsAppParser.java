package com.example.whatsviewer.service;

import com.example.whatsviewer.model.ChatMessage;
import com.example.whatsviewer.model.UploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class WhatsAppParser {

  // Matches WhatsApp export lines like:
  // [30/08/2023, 7:17:12 PM] Ali Mehdi:
  // [30/08/2023, 7:17 PM] Ali Mehdi:
  private static final Pattern HEADER_PATTERN = Pattern.compile(
      "^\\[(\\d{1,2})/(\\d{1,2})/(\\d{2,4}),\\s*(\\d{1,2}:\\d{2})(?::(\\d{2}))?\\s*(AM|PM)\\]\\s*(.*?):\\s*$",
      Pattern.CASE_INSENSITIVE
  );

  // Matches attachment lines like:
  // <attached: 00000003-PHOTO-2023-08-30-19-17-12.jpg>
  private static final Pattern ATTACH_PATTERN = Pattern.compile(
      "^<attached:\\s*(.+?)>\\s*$",
      Pattern.CASE_INSENSITIVE
  );

  private static final DateTimeFormatter TS_OUT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  public UploadResponse parse(MultipartFile file, String myName) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("No file uploaded");
    }

    String original = Optional.ofNullable(file.getOriginalFilename()).orElse("chat");
    String chatName = original;

    String txt;
    try {
      if (original.toLowerCase().endsWith(".zip")) {
        txt = readTxtFromZip(file.getInputStream());
      } else {
        txt = readAll(file.getInputStream(), guessCharset(file));
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not read uploaded file", e);
    }

    List<ChatMessage> messages = parseExportText(txt, myName);
    return new UploadResponse(chatName, myName, messages);
  }

  private static Charset guessCharset(MultipartFile file) {
    // Most WhatsApp exports are UTF-8. Some are UTF-16.
    // We'll try UTF-8 first (in readAll); if it looks broken, dev team can extend.
    return StandardCharsets.UTF_8;
  }

  private static String readTxtFromZip(InputStream zipStream) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zipStream))) {
      ZipEntry entry;
      byte[] buffer = new byte[8192];
      while ((entry = zis.getNextEntry()) != null) {
        String name = entry.getName();
        if (name != null && name.toLowerCase().endsWith(".txt")) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          int n;
          while ((n = zis.read(buffer)) > 0) {
            out.write(buffer, 0, n);
          }
          // try UTF-8 first
          return out.toString(StandardCharsets.UTF_8);
        }
      }
    }
    throw new IllegalArgumentException("ZIP did not contain a .txt chat export");
  }

  private static String readAll(InputStream in, Charset cs) throws IOException {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(in, cs))) {
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append('\n');
      }
      return sb.toString();
    }
  }

  private static List<ChatMessage> parseExportText(String text, String myName) {
    List<ChatMessage> out = new ArrayList<>();

    String[] lines = text.replace("\r\n", "\n").split("\n");

    ChatMessage current = null;
    StringBuilder currentText = new StringBuilder();

    for (String raw : lines) {
      String line = raw == null ? "" : raw.trim();

      Matcher hm = HEADER_PATTERN.matcher(line);
      if (hm.matches()) {
        // flush previous
        flushCurrent(out, current, currentText);

        int dd = Integer.parseInt(hm.group(1));
        int mm = Integer.parseInt(hm.group(2));
        String yyyyStr = hm.group(3);
        int yyyy = yyyyStr.length() == 2 ? 2000 + Integer.parseInt(yyyyStr) : Integer.parseInt(yyyyStr);

        String hhmm = hm.group(4); // 7:17
        String ss = hm.group(5);   // optional
        String ampm = hm.group(6);
        String sender = hm.group(7).trim();

        LocalDateTime ts = parseTs(dd, mm, yyyy, hhmm, ss, ampm);

        current = new ChatMessage();
        current.setTimestamp(ts);
        current.setSender(sender);

        boolean outgoing = myName != null && !myName.isBlank() && sender.equalsIgnoreCase(myName.trim());
        current.setOutgoing(outgoing);

        currentText.setLength(0);
        continue;
      }

      Matcher am = ATTACH_PATTERN.matcher(line);
      if (am.matches() && current != null) {
        String fileName = am.group(1).trim();
        current.setAttachmentName(fileName);
        current.setAttachmentType(guessAttachmentType(fileName));
        // keep text empty unless later lines add text
        continue;
      }

      // normal text line
      if (current != null) {
        if (!currentText.isEmpty()) currentText.append('\n');
        currentText.append(raw); // keep original spacing
      }
    }

    flushCurrent(out, current, currentText);
    return out;
  }

  private static void flushCurrent(List<ChatMessage> out, ChatMessage current, StringBuilder currentText) {
    if (current == null) return;
    String txt = currentText.toString().trim();
    current.setText(txt.isEmpty() ? null : txt);
    out.add(current);
  }

  private static LocalDateTime parseTs(int dd, int mm, int yyyy, String hhmm, String ss, String ampm) {
    String[] parts = hhmm.split(":");
    int h = Integer.parseInt(parts[0]);
    int m = Integer.parseInt(parts[1]);
    int s = (ss == null || ss.isBlank()) ? 0 : Integer.parseInt(ss);

    String ap = ampm.toUpperCase(Locale.ROOT);
    if ("PM".equals(ap) && h < 12) h += 12;
    if ("AM".equals(ap) && h == 12) h = 0;

    return LocalDateTime.of(yyyy, mm, dd, h, m, s);
  }

  private static String guessAttachmentType(String fileName) {
    String n = fileName.toLowerCase(Locale.ROOT);
    if (n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".webp") || n.endsWith(".gif")) {
      return "image";
    }
    if (n.endsWith(".opus") || n.endsWith(".mp3") || n.endsWith(".m4a") || n.endsWith(".wav") || n.endsWith(".ogg")) {
      return "audio";
    }
    return "file";
  }
}

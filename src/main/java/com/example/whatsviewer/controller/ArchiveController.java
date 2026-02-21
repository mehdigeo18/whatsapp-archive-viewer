package com.example.whatsviewer.controller;

import com.example.whatsviewer.model.ChatMessage;
import com.example.whatsviewer.model.UploadResponse;
import com.example.whatsviewer.service.WhatsAppParser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/archive")
@CrossOrigin(origins = "*")
public class ArchiveController {

    private final WhatsAppParser parser;

    public ArchiveController(WhatsAppParser parser) {
        this.parser = parser;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return new UploadResponse("No file uploaded", 0, List.of());
        }

        String name = (file.getOriginalFilename() == null) ? "" : file.getOriginalFilename().toLowerCase();

        String chatText;

        // Support .zip that contains a .txt WhatsApp export
        if (name.endsWith(".zip")) {
            chatText = extractFirstTxtFromZip(file.getBytes());
            if (chatText == null) {
                return new UploadResponse("ZIP uploaded but no .txt found inside", 0, List.of());
            }
        } else {
            // Read as UTF-8 to correctly handle WhatsApp exports (which often include Unicode direction marks)
            chatText = new String(file.getBytes(), StandardCharsets.UTF_8);
        }

        List<ChatMessage> messages = parser.parse(chatText);

        return new UploadResponse("File processed successfully", messages.size(), messages);
    }

    private String extractFirstTxtFromZip(byte[] zipBytes) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String n = entry.getName().toLowerCase();
                if (n.endsWith(".txt")) {
                    byte[] data = zis.readAllBytes();
                    return new String(data, StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }
}

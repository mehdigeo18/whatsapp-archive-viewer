package com.example.whatsviewer.controller;

import com.example.whatsviewer.model.UploadResponse;
import com.example.whatsviewer.service.UploadStore;
import com.example.whatsviewer.service.WhatsAppParser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ArchiveController {

    private final WhatsAppParser parser;
    private final UploadStore uploadStore;

    public ArchiveController(WhatsAppParser parser, UploadStore uploadStore) {
        this.parser = parser;
        this.uploadStore = uploadStore;
    }

    @PostMapping(value = "/api/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "myName", required = false) String myName
    ) {
        try {
            // 1) Parse chat (your existing behavior)
            UploadResponse res = parser.parse(file, myName);

            // 2) If ZIP, store bytes so MediaController can stream attachments later
            String original = file.getOriginalFilename();
            if (original != null && original.toLowerCase().endsWith(".zip")) {
                byte[] zipBytes = file.getBytes();
                String uploadId = uploadStore.put(zipBytes);
                res.setUploadId(uploadId);
            } else {
                // If user uploads .txt directly, there is no media to serve
                res.setUploadId(null);
            }

            return res;
        } catch (Exception e) {
            // Keep it simple for demo: throw a 500 with message
            throw new RuntimeException("Upload/parse failed: " + e.getMessage(), e);
        }
    }

    @GetMapping("/api/health")
    public String health() {
        return "ok";
    }
}

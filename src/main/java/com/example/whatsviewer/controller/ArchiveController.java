package com.example.whatsviewer.controller;

import com.example.whatsviewer.model.UploadResponse;
import com.example.whatsviewer.service.UploadStore;
import com.example.whatsviewer.service.WhatsAppParser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ArchiveController {

    private final WhatsAppParser parser;
    private final UploadStore uploadStore;

    public ArchiveController(WhatsAppParser parser, UploadStore uploadStore) {
        this.parser = parser;
        this.uploadStore = uploadStore;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "myName", required = false) String myName
    ) {
        try {
            UploadResponse res = parser.parse(file, myName);

            String original = file.getOriginalFilename();
            if (original != null && original.toLowerCase().endsWith(".zip")) {
                byte[] zipBytes = file.getBytes();
                String uploadId = uploadStore.put(zipBytes, original);
                res.setUploadId(uploadId);
            } else {
                res.setUploadId(null);
            }

            return res;
        } catch (Exception e) {
            throw new RuntimeException("Upload/parse failed: " + e.getMessage(), e);
        }
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}

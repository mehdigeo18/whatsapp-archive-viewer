package com.example.whatsviewer.controller;

import com.example.whatsviewer.model.ChatMessage;
import com.example.whatsviewer.model.UploadResponse;
import com.example.whatsviewer.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/archive")
@CrossOrigin
public class ArchiveController {

    @Autowired
    private ArchiveService archiveService;

    @PostMapping("/upload")
    public UploadResponse upload(@RequestParam("file") MultipartFile file) throws IOException {

        String content = new String(file.getBytes());
        List<ChatMessage> messages = archiveService.processChat(content);

        return new UploadResponse("File processed successfully", messages.size());
    }
}

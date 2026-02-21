package com.example.whatsviewer.controller;

import com.example.whatsviewer.model.UploadResponse;
import com.example.whatsviewer.service.WhatsAppParser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ArchiveController {

  private final WhatsAppParser parser;

  public ArchiveController(WhatsAppParser parser) {
    this.parser = parser;
  }

  @PostMapping(value = "/api/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public UploadResponse upload(@RequestParam("file") MultipartFile file,
                               @RequestParam(value = "myName", required = false) String myName) {
    return parser.parse(file, myName);
  }

  @GetMapping("/api/health")
  public String health() {
    return "ok";
  }
}

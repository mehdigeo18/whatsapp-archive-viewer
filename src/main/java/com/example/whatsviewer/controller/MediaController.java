package com.example.whatsviewer.controller;

import com.example.whatsviewer.service.UploadStore;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api")
public class MediaController {

    private final UploadStore uploadStore;

    public MediaController(UploadStore uploadStore) {
        this.uploadStore = uploadStore;
    }

    @GetMapping("/media/{uploadId}")
    public ResponseEntity<byte[]> getMedia(
            @PathVariable String uploadId,
            @RequestParam("name") String name
    ) throws Exception {

        var stored = uploadStore.get(uploadId);
        if (stored == null) {
            return ResponseEntity.notFound().build();
        }

        String fileName = URLDecoder.decode(name, StandardCharsets.UTF_8);

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(stored.zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();

                // WhatsApp zips often store files in a folder; match by "endsWith"
                if (!entry.isDirectory() && (entryName.equals(fileName) || entryName.endsWith("/" + fileName))) {

                    byte[] bytes = FileCopyUtils.copyToByteArray(zis);

                    String contentType = guessContentType(fileName);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CACHE_CONTROL, "no-store")
                            .contentType(MediaType.parseMediaType(contentType))
                            .body(bytes);
                }
            }
        }

        return ResponseEntity.notFound().build();
    }

    private String guessContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".opus")) return "audio/ogg";     // OPUS usually in OGG container
        if (lower.endsWith(".ogg")) return "audio/ogg";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".m4a")) return "audio/mp4";
        if (lower.endsWith(".mp4")) return "video/mp4";
        return "application/octet-stream";
    }
}

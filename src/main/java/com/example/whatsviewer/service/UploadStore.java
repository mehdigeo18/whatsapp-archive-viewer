package com.example.whatsviewer.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UploadStore {

    public static class StoredUpload {
        public final byte[] zipBytes;
        public final String originalFileName;
        public final Instant createdAt;

        public StoredUpload(byte[] zipBytes, String originalFileName) {
            this.zipBytes = zipBytes;
            this.originalFileName = originalFileName;
            this.createdAt = Instant.now();
        }
    }

    private final Map<String, StoredUpload> store = new ConcurrentHashMap<>();

    /** Save the uploaded ZIP bytes in memory and return an uploadId */
    public String put(byte[] zipBytes, String originalFileName) {
        String id = UUID.randomUUID().toString();
        store.put(id, new StoredUpload(zipBytes, originalFileName));
        return id;
    }

    /** Retrieve stored upload by id */
    public StoredUpload get(String uploadId) {
        return store.get(uploadId);
    }

    /** Optional: clear one upload */
    public void remove(String uploadId) {
        store.remove(uploadId);
    }

    /** Optional: clear all */
    public void clear() {
        store.clear();
    }
}

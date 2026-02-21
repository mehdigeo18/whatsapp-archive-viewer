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
        public final Instant createdAt;

        public StoredUpload(byte[] zipBytes) {
            this.zipBytes = zipBytes;
            this.createdAt = Instant.now();
        }
    }

    private final Map<String, StoredUpload> store = new ConcurrentHashMap<>();

    public String put(byte[] zipBytes) {
        String id = UUID.randomUUID().toString();
        store.put(id, new StoredUpload(zipBytes));
        return id;
    }

    public StoredUpload get(String id) {
        return store.get(id);
    }

    public void delete(String id) {
        store.remove(id);
    }
}

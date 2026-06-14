package com.example.dhap.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class FileService {

    @Value("${dhap.upload.dir:uploads}")
    private String uploadDir;

    @Value("${dhap.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Saves the uploaded file and returns its publicly accessible URL.
     *
     * Storage layout: uploads/{year}/{month}/{uuid}_{sanitised-original-name}
     * Served at:      {baseUrl}/files/{year}/{month}/{uuid}_{sanitised-name}
     *
     * Proof upload flow (v2 contract):
     *   1. POST /files/upload → { "url": "http://localhost:8080/files/2026/06/abc_proof.jpg" }
     *   2. PATCH /tasks/{id}  → { "proofs": [{ "message": "...", "mediaPaths": ["<url>"] }] }
     */
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must not be empty");
        }

        // Proofs are photos/videos. Anything else (html, svg, exe…) would be
        // served back from /files/** — stored-XSS / malware risk.
        String contentType = file.getContentType();
        if (contentType == null
                || !(contentType.startsWith("image/") || contentType.startsWith("video/"))
                || contentType.equals("image/svg+xml")) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only image and video files are allowed");
        }

        String original  = sanitise(file.getOriginalFilename());
        String stored    = UUID.randomUUID().toString().replace("-", "") + "_" + original;

        LocalDate today  = LocalDate.now();
        String subDir    = today.getYear() + "/" + String.format("%02d", today.getMonthValue());
        Path   targetDir = Paths.get(uploadDir, subDir);

        try {
            Files.createDirectories(targetDir);
            Files.copy(file.getInputStream(), targetDir.resolve(stored));
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file: " + e.getMessage());
        }

        return baseUrl + "/files/" + subDir + "/" + stored;
    }

    /** Strips path separators and keeps only safe filename characters. */
    private String sanitise(String name) {
        if (name == null || name.isBlank()) return "file";
        // Take only the final segment (guards against path traversal like ../../etc/passwd)
        String base = Paths.get(name).getFileName().toString();
        // Replace anything that isn't alphanumeric, dot, dash, or underscore
        return base.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
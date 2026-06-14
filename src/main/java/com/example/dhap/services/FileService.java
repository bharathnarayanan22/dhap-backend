package com.example.dhap.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    private final Cloudinary cloudinary;

    public FileService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Uploads the file to Cloudinary and returns its publicly accessible (CDN) URL.
     *
     * Uploaded under the "dhap/" folder with a unique public id, so files survive
     * server restarts/redeploys (unlike local disk on ephemeral hosts like Render).
     *
     * Proof upload flow (v2 contract):
     *   1. POST /files/upload → { "url": "https://res.cloudinary.com/<cloud>/.../dhap/abc_proof.jpg" }
     *   2. PATCH /tasks/{id}  → { "proofs": [{ "message": "...", "mediaPaths": ["<url>"] }] }
     */
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must not be empty");
        }

        // Proofs are photos/videos. Anything else (html, svg, exe…) would be
        // served back as a public URL — stored-XSS / malware risk.
        String contentType = file.getContentType();
        if (contentType == null
                || !(contentType.startsWith("image/") || contentType.startsWith("video/"))
                || contentType.equals("image/svg+xml")) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only image and video files are allowed");
        }

        String original = sanitise(file.getOriginalFilename());
        String publicId = UUID.randomUUID().toString().replace("-", "") + "_" + original;

        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",   // handles both images and video
                            "folder", "dhap",
                            "public_id", publicId));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file: " + e.getMessage());
        }
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

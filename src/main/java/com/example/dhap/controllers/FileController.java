package com.example.dhap.controllers;

import com.example.dhap.services.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * POST /files/upload  (multipart/form-data, field name: "file")
     *
     * Returns 201 → { "url": "http://localhost:8080/files/2026/06/abc_proof.jpg" }
     *
     * Use the returned URL as a mediaPaths entry when submitting task proof:
     *   PATCH /tasks/{id}
     *   { "status": "IN_VERIFICATION",
     *     "proofs": [{ "message": "Delivered", "mediaPaths": ["<url>"] }] }
     *
     * No auth required for GET /files/** (served statically by WebConfig).
     * This POST endpoint does require a valid JWT.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file) {
        String url = fileService.store(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", url));
    }
}
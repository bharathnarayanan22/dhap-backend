package com.example.dhap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves uploaded proof files statically at GET /files/**.
 *
 * Files stored at  → {dhap.upload.dir}/2026/06/uuid_filename.jpg
 * Served at        → http://localhost:8080/files/2026/06/uuid_filename.jpg
 *
 * Do NOT use @EnableWebMvc — it would override Spring Boot's auto-configuration.
 * WebMvcConfigurer adds to it instead.
 *
 * For production: replace the local directory with S3/GCS and remove this handler.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${dhap.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "file:" prefix resolves relative to the JVM working directory (project root
        // when running with ./gradlew bootRun). Trailing slash is required.
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
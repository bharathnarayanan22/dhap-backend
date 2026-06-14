package com.example.dhap.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the Cloudinary client from the CLOUDINARY_URL connection string
 * (format: cloudinary://<api_key>:<api_secret>@<cloud_name>).
 *
 * Set via the CLOUDINARY_URL env var — locally through the .env file,
 * on Render through the dashboard's environment variables.
 */
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.url}")
    private String cloudinaryUrl;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(cloudinaryUrl);
    }
}

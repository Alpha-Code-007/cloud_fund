package com.donorbox.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded images as static content
        registry.addResourceHandler("/api/images/**")//causes,event image access,blog
                .addResourceLocations("file:" + Paths.get(uploadDir).toAbsolutePath().toString() + "/")
                .setCachePeriod(3600); // Cache for 1 hour
        
        // Serve personal-causes files as static content
        registry.addResourceHandler("/personal-causes/**")
                .addResourceLocations("file:" + Paths.get(uploadDir).toAbsolutePath().toString() + "/")
                .setCachePeriod(3600); // Cache for 1 hour
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Get CORS configuration from environment variables
        String allowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        String allowedMethods = System.getenv("CORS_ALLOWED_METHODS");
        String allowedHeaders = System.getenv("CORS_ALLOWED_HEADERS");
        String allowCredentials = System.getenv("CORS_ALLOW_CREDENTIALS");
        String maxAge = System.getenv("CORS_MAX_AGE");
        
        // Only configure CORS if environment variables are provided
        if (allowedOrigins != null && !allowedOrigins.trim().isEmpty()) {
            registry.addMapping("/**")
                    .allowedOriginPatterns(allowedOrigins.split(","))
                    .allowedMethods(allowedMethods != null ? allowedMethods.split(",") : 
                        new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"})
                    .allowedHeaders(allowedHeaders != null ? allowedHeaders.split(",") : 
                        new String[]{"Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With"})
                    .exposedHeaders("Access-Control-Allow-Origin")
                    .allowCredentials("true".equalsIgnoreCase(allowCredentials))
                    .maxAge(maxAge != null ? Long.parseLong(maxAge) : 3600);
        } else {
            // No CORS configuration - log warning
            System.out.println("WARNING: No CORS_ALLOWED_ORIGINS environment variable found. CORS will be disabled for security.");
        }
    }
}

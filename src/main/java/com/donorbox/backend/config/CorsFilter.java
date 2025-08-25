package com.donorbox.backend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // Get the origin from the request
        String origin = request.getHeader("Origin");
        String method = request.getMethod();
        String requestURI = request.getRequestURI();
        
        log.info("CORS Filter - Origin: {}, Method: {}, URI: {}", origin, method, requestURI);
        
        // Skip CORS filtering for static resources (images, videos, documents)
        if (requestURI.startsWith("/api/images/") || 
            requestURI.startsWith("/api/documents/") || 
            requestURI.startsWith("/api/media/") || 
            requestURI.startsWith("/api/causes/") || 
            requestURI.startsWith("/uploads/") ||
            requestURI.startsWith("/personal-causes/") ||
            requestURI.startsWith("/swagger-ui/") ||
            requestURI.startsWith("/v3/api-docs/") ||
            requestURI.startsWith("/webjars/")) {
            log.info("CORS Filter - Skipping CORS for static resource: {}", requestURI);
            // Add comprehensive CORS headers for static resources to ensure they load properly
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Requested-With, Cache-Control");
            response.setHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin");
            response.setHeader("Access-Control-Max-Age", "3600");
            chain.doFilter(req, res);
            return;
        }
        
        // Simplified CORS configuration for development
        String activeProfile = System.getenv("SPRING_PROFILES_ACTIVE");
        String allowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        String allowedMethods = System.getenv("CORS_ALLOWED_METHODS");
        String allowedHeaders = System.getenv("CORS_ALLOWED_HEADERS");
        String allowCredentials = System.getenv("CORS_ALLOW_CREDENTIALS");
        
        // In development mode, allow everything for easier testing
        if (!"production".equals(activeProfile) && !"prod".equals(activeProfile)) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            log.info("CORS Filter - Development mode: Allowing all origins for: {}", requestURI);
        } else {
            // Production mode - use strict CORS configuration
            if (allowedOrigins != null && !allowedOrigins.trim().isEmpty()) {
                String[] origins = allowedOrigins.split(",");
                boolean originAllowed = false;
                for (String allowedOrigin : origins) {
                    if (origin != null && origin.matches(allowedOrigin.trim().replace("*", ".*"))) {
                        response.setHeader("Access-Control-Allow-Origin", origin);
                        originAllowed = true;
                        log.info("CORS Filter - Production: Setting origin to: {}", origin);
                        break;
                    }
                }
                if (!originAllowed) {
                    log.warn("CORS Filter - Production: Origin not allowed: {}", origin);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            } else {
                log.error("CORS_ALLOWED_ORIGINS not set in production environment. CORS will be disabled for security.");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        
        // Set methods from environment variable or default
        if (allowedMethods != null && !allowedMethods.trim().isEmpty()) {
            response.setHeader("Access-Control-Allow-Methods", allowedMethods);
        } else {
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        }
        
        // Set headers from environment variable or default
        if (allowedHeaders != null && !allowedHeaders.trim().isEmpty()) {
            response.setHeader("Access-Control-Allow-Headers", allowedHeaders);
        } else {
            response.setHeader("Access-Control-Allow-Headers", 
                "Origin, Content-Type, Accept, Authorization, X-Requested-With, Cache-Control");
        }
        
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin");
        
        // Special handling for admin endpoints to ensure they work with Swagger
        if (requestURI.startsWith("/admin/")) {
            response.setHeader("Access-Control-Allow-Headers", 
                "Origin, Content-Type, Accept, Authorization, X-Requested-With, Cache-Control");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        }
        
        // Special handling for personal cause endpoints to ensure they work with Swagger
        if (requestURI.startsWith("/api/personal-cause-submissions/")) {
            response.setHeader("Access-Control-Allow-Headers", 
                "Origin, Content-Type, Accept, Authorization, X-Requested-With, Cache-Control");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            // Ensure personal cause endpoints always get proper CORS headers
            if (response.getHeader("Access-Control-Allow-Origin") == null) {
                response.setHeader("Access-Control-Allow-Origin", "*");
            }
        }
        
        // Set credentials based on environment variable or development mode
        if ("true".equalsIgnoreCase(allowCredentials) || 
            (!"production".equals(activeProfile) && !"prod".equals(activeProfile))) {
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }

        // Handle preflight requests
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.info("CORS Filter - Handling OPTIONS preflight request");
            response.setStatus(HttpServletResponse.SC_OK);
            return; // Don't continue the filter chain for OPTIONS requests
        }
        
        log.info("CORS Filter - Continuing filter chain");
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialization not needed
    }

    @Override
    public void destroy() {
        // Cleanup not needed
    }
}

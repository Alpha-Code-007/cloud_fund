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
        
        // Get CORS configuration from environment variables
        String allowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        String allowedMethods = System.getenv("CORS_ALLOWED_METHODS");
        String allowedHeaders = System.getenv("CORS_ALLOWED_HEADERS");
        String allowCredentials = System.getenv("CORS_ALLOW_CREDENTIALS");
        
        // Set CORS headers based on environment configuration
        if (allowedOrigins != null && !allowedOrigins.trim().isEmpty()) {
            String[] origins = allowedOrigins.split(",");
            boolean originAllowed = false;
            for (String allowedOrigin : origins) {
                if (origin != null && origin.matches(allowedOrigin.trim().replace("*", ".*"))) {
                    response.setHeader("Access-Control-Allow-Origin", origin);
                    originAllowed = true;
                    log.info("CORS Filter - Setting origin to: {}", origin);
                    break;
                }
            }
            if (!originAllowed) {
                log.warn("CORS Filter - Origin not allowed: {}", origin);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } else {
            // No CORS configuration found - reject all requests for security
            log.warn("CORS Filter - No CORS configuration found. Rejecting request from origin: {}", origin);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
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
                "Origin, Content-Type, Accept, Authorization, X-Requested-With");
        }
        
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin");
        
        // Set credentials based on environment variable
        if ("true".equalsIgnoreCase(allowCredentials)) {
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

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

        String origin = request.getHeader("Origin");
        String method = request.getMethod();
        String requestURI = request.getRequestURI();

        // Detect server's own origin
        String serverOrigin = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        log.info("CORS Filter - Origin: {}, Method: {}, URI: {}", origin, method, requestURI);

        //  Skip CORS if request is same-origin
        if (origin != null && origin.equalsIgnoreCase(serverOrigin)) {
            log.info("CORS Filter - Same origin request ({}), skipping CORS headers", serverOrigin);
            chain.doFilter(req, res);
            return;
        }

        // Skip CORS filtering for static resources
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
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers",
                    "Origin, Content-Type, Accept, Authorization, X-Requested-With, Cache-Control");
            response.setHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin");
            response.setHeader("Access-Control-Max-Age", "3600");
            chain.doFilter(req, res);
            return;
        }

        // Env configs
        String activeProfile = System.getenv("SPRING_PROFILES_ACTIVE");
        String allowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        String allowedMethods = System.getenv("CORS_ALLOWED_METHODS");
        String allowedHeaders = System.getenv("CORS_ALLOWED_HEADERS");
        String allowCredentials = System.getenv("CORS_ALLOW_CREDENTIALS");

        // Development mode → allow all
        if (!"production".equals(activeProfile) && !"prod".equals(activeProfile)) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers",
                    "Origin, Content-Type, Accept, Authorization, X-Requested-With, Cache-Control");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin");
            log.info("CORS Filter - Development mode: Allowing all origins for: {}", requestURI);
        } else {
            // Production → strict
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
                log.error("CORS_ALLOWED_ORIGINS not set in production. Blocking request.");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        // Allowed methods
        if (allowedMethods != null && !allowedMethods.trim().isEmpty()) {
            response.setHeader("Access-Control-Allow-Methods", allowedMethods);
        } else {
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        }

        // Allowed headers
        if (allowedHeaders != null && !allowedHeaders.trim().isEmpty()) {
            response.setHeader("Access-Control-Allow-Headers", allowedHeaders);
        } else {
            response.setHeader("Access-Control-Allow-Headers",
                    "Origin, Content-Type, Accept, Authorization, X-Requested-With, Cache-Control");
        }

        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin");

        // Special admin endpoints
        if (requestURI.startsWith("/admin/")) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers",
                    "Origin, Content-Type, Accept, Authorization, X-Requested-With, Cache-Control");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin");
            log.info("CORS Filter - Admin endpoint: Applied headers for {}", requestURI);
        }

        // Special handling: /admin/causes/with-video
        if (requestURI.startsWith("/admin/causes/with-video")) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers",
                    "Origin, Content-Type, Accept, Authorization, X-Requested-With, Cache-Control");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin");

            log.info("CORS Filter - Admin Causes/With-Video: Applied headers for {}", requestURI);

            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                log.info("CORS Filter - Preflight OPTIONS for {}", requestURI);
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }
        }

        // Special handling: /admin/causes/with-media
        if (requestURI.startsWith("/admin/causes/with-media")) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers",
                    "Origin, Content-Type, Accept, Authorization, X-Requested-With, Cache-Control");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin");

            log.info("CORS Filter - Admin Causes/With-Media: Applied headers for {}", requestURI);

            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                log.info("CORS Filter - Preflight OPTIONS for {}", requestURI);
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }
        }

        // Special Swagger/personal-cause
        if (requestURI.startsWith("/api/personal-cause-submissions/") ||
                requestURI.startsWith("/v3/api-docs") ||
                requestURI.startsWith("/swagger-ui") ||
                requestURI.startsWith("/swagger-ui.html")) {

            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers",
                    "Origin, Content-Type, Accept, Authorization, X-Requested-With, Cache-Control");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin");

            log.info("CORS Filter - Swagger/PersonalCause: Applied headers for {}", requestURI);

            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                log.info("CORS Filter - Preflight OPTIONS for {}", requestURI);
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }
        }

        // Credentials setting
        if ("true".equalsIgnoreCase(allowCredentials) ||
                (!"production".equals(activeProfile) && !"prod".equals(activeProfile))) {
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }

        // Global preflight handler
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.info("CORS Filter - Handling OPTIONS preflight request globally");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        log.info("CORS Filter - Continuing filter chain");
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) { }

    @Override
    public void destroy() { }
}

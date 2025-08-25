package com.donorbox.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions().disable()) // For H2 console
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/public/blogs").permitAll()
                .requestMatchers("/api/public/blogs/**").permitAll()
                
                // Static Resources - Allow public access without authentication
                .requestMatchers("/api/images/**").permitAll()  // Allow public access to images
                .requestMatchers("/api/documents/**").permitAll() // Allow public access to documents
                .requestMatchers("/api/media/**").permitAll()    // Allow public access to media
                .requestMatchers("/uploads/**").permitAll()     // Allow direct access to uploads folder
                .requestMatchers("/personal-causes/**").permitAll() // Allow public access to personal causes media
                
                .requestMatchers("/api/personal-cause-submissions/**").permitAll() // Allow public access to personal cause submissions
                .requestMatchers("/donate").permitAll()
                .requestMatchers("/donations").permitAll()
                .requestMatchers("/causes").permitAll()
                .requestMatchers("/causes/**").permitAll()
                .requestMatchers("/events").permitAll()
                .requestMatchers("/blogs").permitAll()
                .requestMatchers("/events/**").permitAll()
                .requestMatchers("/volunteer/register").permitAll()
                .requestMatchers("/contact/send").permitAll()
                .requestMatchers("/homepage-stats").permitAll()
                .requestMatchers("/payment/**").permitAll()
                
                // Swagger endpoints
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                
                // Admin endpoints - require authentication
                .requestMatchers("/admin/**").authenticated()
                
                // H2 Console (for testing)
                .requestMatchers("/h2-console/**").permitAll()
                
                // Root context redirect
                .requestMatchers("/").permitAll()
                .requestMatchers("/health").permitAll()
                
                // Any other request
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {});

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // @Value injection is working correctly now
        log.info("Creating admin user with username: {}", adminUsername);
        
        UserDetails admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder().encode(adminPassword))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Get allowed origins from environment variables
        String allowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        if (allowedOrigins != null && !allowedOrigins.trim().isEmpty()) {
            configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
            log.info("CORS configured with allowed origins: {}", allowedOrigins);
        } else {
            // Check if we're in production environment
            String activeProfile = System.getenv("SPRING_PROFILES_ACTIVE");
            if ("production".equals(activeProfile) || "prod".equals(activeProfile)) {
                // Production environment - reject all requests if no CORS configuration
                log.error("CORS_ALLOWED_ORIGINS not set in production environment. CORS will be disabled for security.");
                configuration.setAllowedOriginPatterns(Arrays.asList()); // Empty list = no origins allowed
            } else {
                // Development environment - allow localhost
                log.info("No CORS_ALLOWED_ORIGINS environment variable found. Using localhost fallback for development.");
                configuration.setAllowedOriginPatterns(Arrays.asList(
                    "http://localhost:*",
                    "https://localhost:*"
                ));
            }
        }
        
        // Get allowed methods from environment variable
        String allowedMethods = System.getenv("CORS_ALLOWED_METHODS");
        if (allowedMethods != null && !allowedMethods.trim().isEmpty()) {
            configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        } else {
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        }
        
        // Get allowed headers from environment variable
        String allowedHeaders = System.getenv("CORS_ALLOWED_HEADERS");
        if (allowedHeaders != null && !allowedHeaders.trim().isEmpty()) {
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        } else {
            configuration.setAllowedHeaders(Arrays.asList(
                "Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With"
            ));
        }
        
        // Only expose necessary headers
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin"));
        
        // Allow credentials - secure by default
        String allowCredentials = System.getenv("CORS_ALLOW_CREDENTIALS");
        String activeProfile = System.getenv("SPRING_PROFILES_ACTIVE");
        if ("production".equals(activeProfile) || "prod".equals(activeProfile)) {
            // Production - only allow credentials if explicitly configured
            configuration.setAllowCredentials("true".equalsIgnoreCase(allowCredentials));
        } else {
            // Development - default to true for convenience
            configuration.setAllowCredentials(allowCredentials == null || "true".equalsIgnoreCase(allowCredentials));
        }
        
        // Set max age from environment variable
        String maxAge = System.getenv("CORS_MAX_AGE");
        if (maxAge != null && !maxAge.trim().isEmpty()) {
            try {
                configuration.setMaxAge(Long.parseLong(maxAge));
            } catch (NumberFormatException e) {
                configuration.setMaxAge(3600L); // Default 1 hour
            }
        } else {
            configuration.setMaxAge(3600L);
        }
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

package com.donorbox.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Bean
    public OpenAPI donorboxOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl(baseUrl);
        localServer.setDescription("Server URL for Local Development");

        // Create security scheme for HTTP Basic Authentication
        SecurityScheme basicAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic")
                .name("basicAuth")
                .description("HTTP Basic Authentication.");

        // Create security requirement
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("basicAuth");

        Info info = new Info()
                .title("DonorBox API")
                .description("Backend API for DonorBox Crowdfunding Platform")
                .version("1.0.0")
                .contact(new io.swagger.v3.oas.models.info.Contact()
                        .name("DonorBox Team")
                        .email("support@donorbox.com"));

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer))
                .components(new Components().addSecuritySchemes("basicAuth", basicAuthScheme))
                .addSecurityItem(securityRequirement);
    }
}

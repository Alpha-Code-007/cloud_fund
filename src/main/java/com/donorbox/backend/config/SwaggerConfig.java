package com.donorbox.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI donorboxOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Server URL for Local Development");


        Info info = new Info()
                .title("Donorbox Crowdfunding Platform API")
                .version("1.0")
                .description("API documentation for the Donorbox crowdfunding platform backend with international payment support")
              ;

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}

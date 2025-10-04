package com.example.fitnesstracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fitness Tracker API")
                        .version("1.0.0")
                        .description("API REST para gestión de usuarios, ejercicios y rutinas de entrenamiento")
                        .contact(new Contact()
                                .name("Franco Moreal")
                                .email("francomoreal@gmail.com")
                                .url("https://github.com/FrancoMoreal/fitnesstracker"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desarrollo"),
                        new Server()
                                .url("https://api.fitnesstracker.com")
                                .description("Servidor de producción (cuando lo despliegues)")
                ));
    }
}
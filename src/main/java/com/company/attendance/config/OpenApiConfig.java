package com.company.attendance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger UI configuration for the Construction Attendance Engine.
 *
 * <p>Provides API metadata, contact information, and server definitions
 * exposed at {@code /swagger-ui.html} and {@code /api-docs}.</p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Builds the custom {@link OpenAPI} specification bean.
     *
     * @return the configured OpenAPI descriptor
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Construction Attendance & Overtime Settlement Engine")
                        .version("1.0.0")
                        .description("Enterprise-grade REST API for managing construction worker attendance, "
                                + "real-time tracking, automatic overtime calculation, and monthly overtime settlement.")
                        .contact(new Contact()
                                .name("Engineering Team")
                                .email("engineering@company.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://company.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development"),
                        new Server().url("https://api.company.com").description("Production")));
    }
}

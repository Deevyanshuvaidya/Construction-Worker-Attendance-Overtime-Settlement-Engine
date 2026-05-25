package com.company.attendance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for the Construction Attendance Engine.
 *
 * <p>Configures global CORS mappings so that all {@code /api/**} endpoints
 * accept cross-origin requests from any origin during development. In
 * production, the allowed origins should be restricted to trusted domains.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Registers CORS mappings for all API endpoints.
     *
     * @param registry the {@link CorsRegistry} to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}

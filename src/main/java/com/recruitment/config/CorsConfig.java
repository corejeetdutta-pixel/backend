package com.recruitment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(
                "http://localhost:5173",
                "https://rec-2025-core.web.app",
                "https://rec-2025-core.firebaseapp.com",
                "https://atract.in",
                "https://www.atract.in"

            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("Set-Cookie")  // Important for cookies
            .allowCredentials(true)
            .maxAge(3600);
    }
}

package fr.cnrs.opentheso.config;

import fr.cnrs.opentheso.listeners.ApiKeyInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    @Autowired
    private ApiKeyInterceptor apiKeyInterceptor; // ✅ injecter ton interceptor API Key

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 1️⃣ LoggingInterceptor sur toutes les routes sauf ressources statiques et Swagger
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/resources/**",
                        "/static/**",
                        "/d3js/**",
                        "/.well-known/**",
                        "/v3/**",
                        "/swagger-ui/**"
                );

        // 2️⃣ ApiKeyInterceptor sur toutes les routes /api/**
        // uniquement pour POST, PUT, DELETE (déjà géré dans l’interceptor)
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/api/**");
    }

    // CORS
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }
        };
    }
}

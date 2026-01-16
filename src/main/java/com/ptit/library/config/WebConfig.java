package com.ptit.library.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for REST API
 * No longer uses session interceptor or static resource handlers
 * as frontend is now separate from backend
 */
@Configuration
@EnableAsync
public class WebConfig implements WebMvcConfigurer {

        @Bean
        public Gson gson() {
                return new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                                .create();
        }

        @Override
        public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                                .allowedOrigins("*")
                                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                                .allowedHeaders("*")
                                .exposedHeaders("Authorization");

                registry.addMapping("/ws/**")
                                .allowedOrigins("*")
                                .allowedMethods("GET", "POST")
                                .allowedHeaders("*");
        }
}

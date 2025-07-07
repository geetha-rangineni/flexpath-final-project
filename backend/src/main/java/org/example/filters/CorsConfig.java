// Package declaration
package org.example.filters;

// Required Spring Framework imports
import org.springframework.context.annotation.Bean;                     // Used to define Spring-managed beans
import org.springframework.context.annotation.Configuration;          // Marks this class as a configuration class
import org.springframework.web.servlet.config.annotation.CorsRegistry; // Used to configure CORS
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // Provides custom MVC configuration

/**
 * Configuration class to define CORS (Cross-Origin Resource Sharing) rules.
 * Enables the frontend (e.g., React app on localhost:5173) to access backend APIs.
 */
@Configuration
public class CorsConfig {

    /**
     * Defines a Spring bean that customizes CORS configuration.
     * This method returns a WebMvcConfigurer that applies CORS rules globally.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            /**
             * Configures CORS mappings for all endpoints.
             * Allows specific origins, HTTP methods, headers, and credentials.
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Apply CORS rules to all paths
                        .allowedOrigins("http://localhost:5173") // Allow requests from this frontend origin
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow common HTTP methods
                        .allowedHeaders("*") // Allow all headers (Authorization, Content-Type, etc.)
                        .allowCredentials(true); // Allow sending cookies/auth headers (credentials)
            }
        };
    }
}

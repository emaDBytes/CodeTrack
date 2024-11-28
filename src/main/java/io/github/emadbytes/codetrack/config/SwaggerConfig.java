// src\main\java\io\github\emadbytes\codetrack\config\SwaggerConfig.java
package io.github.emadbytes.codetrack.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot API Documentation")
                        .version("1.0.0")
                        .description("This is the documentation for the Spring Boot API."));
    }
}
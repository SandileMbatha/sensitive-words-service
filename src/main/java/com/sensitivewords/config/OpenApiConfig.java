package com.sensitivewords.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sensitiveWordsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sensitive Words Service API")
                        .description("Manages sensitive words and sanitizes messages that contain them")
                        .version("1.0.0")
                        .contact(new Contact().name("Sandile Mbatha")));
    }
}

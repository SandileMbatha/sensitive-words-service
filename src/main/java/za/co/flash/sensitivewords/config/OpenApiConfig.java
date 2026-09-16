package za.co.flash.sensitivewords.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata (title, description, version) shown on the Swagger UI page.
 */
@Configuration
public class OpenApiConfig {

    /**
     * @return the OpenAPI bean springdoc uses to build the Swagger UI and the {@code /v3/api-docs} document
     */
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

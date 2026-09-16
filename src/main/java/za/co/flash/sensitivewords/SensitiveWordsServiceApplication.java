package za.co.flash.sensitivewords;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Entry point for the Sensitive Words microservice.
 * Caching is enabled so the sensitive word list can be kept in memory - see
 * {@link za.co.flash.sensitivewords.service.SanitizeService}.
 */
@SpringBootApplication
@EnableCaching
public class SensitiveWordsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SensitiveWordsServiceApplication.class, args);
    }
}

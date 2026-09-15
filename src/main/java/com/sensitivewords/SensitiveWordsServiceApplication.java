package com.sensitivewords;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SensitiveWordsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SensitiveWordsServiceApplication.class, args);
    }
}

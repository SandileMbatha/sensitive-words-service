package com.sensitivewords.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body used to create or update a sensitive word")
public record SensitiveWordRequest(

        @Schema(description = "The word or phrase to sanitize", example = "SELECT")
        @NotBlank(message = "word must not be blank")
        @Size(max = 255, message = "word must be at most 255 characters")
        String word
) {
}

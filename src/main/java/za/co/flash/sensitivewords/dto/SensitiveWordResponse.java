package za.co.flash.sensitivewords.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Sensitive word as returned by the CRUD endpoints.
 */
@Schema(description = "Sensitive word as stored in the database")
public record SensitiveWordResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "SELECT")
        String word,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}

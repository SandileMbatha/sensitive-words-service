package za.co.flash.sensitivewords.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Standard error response returned for all failed requests.
 */
@Schema(description = "Standard error response returned for all failed requests")
public record ErrorResponse(

        LocalDateTime timestamp,

        @Schema(example = "404")
        int status,

        @Schema(example = "Not Found")
        String error,

        @Schema(example = "Sensitive word with id 99 not found")
        String message,

        @Schema(example = "/api/v1/sensitive-words/99")
        String path
) {
}

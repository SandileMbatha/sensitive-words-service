package za.co.flash.sensitivewords.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Standard error response returned for all failed requests.
 */
@Schema(description = "Standard error response returned for all failed requests")
public record ErrorResponse(

        LocalDateTime timestamp,

        @Schema(description = "The HTTP status code", example = "404")
        int statusCode,

        @Schema(description = "The HTTP status reason phrase", example = "Not Found")
        String error,

        @Schema(description = "Detail explaining what went wrong", example = "Sensitive word with id 99 not found")
        String message,

        @Schema(description = "The request path that caused the error", example = "/api/v1/sensitive-words/99")
        String path
) {
}

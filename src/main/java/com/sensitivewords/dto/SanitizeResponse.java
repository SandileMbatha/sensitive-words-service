package com.sensitivewords.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response body containing the sanitized message")
public record SanitizeResponse(

        @Schema(example = "SELECT * FROM sensitiveWords")
        String originalMessage,

        @Schema(description = "The message with every sensitive word replaced by asterisks",
                example = "****** * FROM sensitiveWords")
        String sanitizedMessage
) {
}

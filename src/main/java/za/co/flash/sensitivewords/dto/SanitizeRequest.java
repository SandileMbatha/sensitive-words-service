package za.co.flash.sensitivewords.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/v1/sanitize}.
 */
@Schema(description = "Request body containing the raw message that needs to be sanitized")
public record SanitizeRequest(

        @Schema(description = "The raw message received from the client application",
                example = "SELECT * FROM sensitiveWords")
        @NotBlank(message = "message must not be blank")
        String message
) {
}

package com.sensitivewords.controller;

import com.sensitivewords.dto.ApiError;
import com.sensitivewords.dto.SanitizeRequest;
import com.sensitivewords.dto.SanitizeResponse;
import com.sensitivewords.service.SanitizeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Business logic endpoint used by client applications to sanitize a message before it is displayed.
 */
@RestController
@RequestMapping("/api/v1/sanitize")
@RequiredArgsConstructor
@Tag(name = "Sanitize", description = "External API used to star out sensitive words in a message")
public class SanitizeController {

    private final SanitizeService sanitizeService;

    @PostMapping
    @Operation(summary = "Sanitize a message",
            description = "Replaces every word in the message that appears on the sensitive words list with asterisks")
    @ApiResponse(responseCode = "200", description = "Message sanitized")
    @ApiResponse(responseCode = "400", description = "Validation failure",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<SanitizeResponse> sanitize(@Valid @RequestBody SanitizeRequest request) {
        String sanitizedMessage = sanitizeService.sanitize(request.message());
        return ResponseEntity.ok(new SanitizeResponse(request.message(), sanitizedMessage));
    }
}

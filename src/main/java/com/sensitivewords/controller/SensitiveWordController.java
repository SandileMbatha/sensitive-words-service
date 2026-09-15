package com.sensitivewords.controller;

import com.sensitivewords.dto.ApiError;
import com.sensitivewords.dto.SensitiveWordRequest;
import com.sensitivewords.dto.SensitiveWordResponse;
import com.sensitivewords.service.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CRUD endpoints used to manage the sensitive words list.
 * Intended for internal/admin consumption only, e.g. a back-office tool.
 */
@RestController
@RequestMapping("/api/v1/sensitive-words")
@RequiredArgsConstructor
@Tag(name = "Sensitive Words", description = "Internal CRUD API used to manage the list of sensitive words")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    @PostMapping
    @Operation(summary = "Create a sensitive word", description = "Adds a new word/phrase to the sensitive words list")
    @ApiResponse(responseCode = "201", description = "Word created")
    @ApiResponse(responseCode = "400", description = "Validation failure",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Word already exists",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<SensitiveWordResponse> create(@Valid @RequestBody SensitiveWordRequest request) {
        SensitiveWordResponse response = sensitiveWordService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List sensitive words", description = "Returns every word on the sensitive words list, ordered alphabetically")
    @ApiResponse(responseCode = "200", description = "List of sensitive words")
    public ResponseEntity<List<SensitiveWordResponse>> getAll() {
        return ResponseEntity.ok(sensitiveWordService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a sensitive word by id")
    @ApiResponse(responseCode = "200", description = "Word found")
    @ApiResponse(responseCode = "404", description = "Word not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<SensitiveWordResponse> getById(
            @Parameter(description = "Id of the sensitive word", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(sensitiveWordService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a sensitive word")
    @ApiResponse(responseCode = "200", description = "Word updated")
    @ApiResponse(responseCode = "404", description = "Word not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Another word with the same value already exists",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<SensitiveWordResponse> update(
            @Parameter(description = "Id of the sensitive word", example = "1") @PathVariable Long id,
            @Valid @RequestBody SensitiveWordRequest request) {
        return ResponseEntity.ok(sensitiveWordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a sensitive word")
    @ApiResponse(responseCode = "204", description = "Word deleted")
    @ApiResponse(responseCode = "404", description = "Word not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id of the sensitive word", example = "1") @PathVariable Long id) {
        sensitiveWordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

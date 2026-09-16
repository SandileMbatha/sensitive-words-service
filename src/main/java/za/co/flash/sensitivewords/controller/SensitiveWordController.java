package za.co.flash.sensitivewords.controller;

import za.co.flash.sensitivewords.dto.ErrorResponse;
import za.co.flash.sensitivewords.dto.SensitiveWordRequest;
import za.co.flash.sensitivewords.dto.SensitiveWordResponse;
import za.co.flash.sensitivewords.service.SensitiveWordService;
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

    /**
     * Creates a new sensitive word.
     *
     * @param request the word to add
     * @return the created word
     * @throws za.co.flash.sensitivewords.exception.DuplicateSensitiveWordException if the word already exists
     */
    @PostMapping
    @Operation(summary = "Create a sensitive word", description = "Adds a new word/phrase to the sensitive words list")
    @ApiResponse(responseCode = "201", description = "Word created")
    @ApiResponse(responseCode = "400", description = "Validation failure",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Word already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<SensitiveWordResponse> createSensitiveWord(@Valid @RequestBody SensitiveWordRequest request) {
        SensitiveWordResponse response = sensitiveWordService.createSensitiveWord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * @return every sensitive word, ordered alphabetically
     */
    @GetMapping
    @Operation(summary = "List sensitive words", description = "Returns every word on the sensitive words list, ordered alphabetically")
    @ApiResponse(responseCode = "200", description = "List of sensitive words")
    public ResponseEntity<List<SensitiveWordResponse>> getAllSensitiveWords() {
        return ResponseEntity.ok(sensitiveWordService.getAllSensitiveWords());
    }

    /**
     * @param id id of the word to fetch
     * @return the matching word
     * @throws za.co.flash.sensitivewords.exception.SensitiveWordNotFoundException if no word has that id
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a sensitive word by id")
    @ApiResponse(responseCode = "200", description = "Word found")
    @ApiResponse(responseCode = "404", description = "Word not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<SensitiveWordResponse> getSensitiveWordById(
            @Parameter(description = "Id of the sensitive word", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(sensitiveWordService.getSensitiveWordById(id));
    }

    /**
     * Updates the value of an existing sensitive word.
     *
     * @param id      id of the word to update
     * @param request the new value
     * @return the updated word
     * @throws za.co.flash.sensitivewords.exception.SensitiveWordNotFoundException  if no word has that id
     * @throws za.co.flash.sensitivewords.exception.DuplicateSensitiveWordException if another word already has the new value
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a sensitive word")
    @ApiResponse(responseCode = "200", description = "Word updated")
    @ApiResponse(responseCode = "404", description = "Word not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Another word with the same value already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<SensitiveWordResponse> updateSensitiveWord(
            @Parameter(description = "Id of the sensitive word", example = "1") @PathVariable Long id,
            @Valid @RequestBody SensitiveWordRequest request) {
        return ResponseEntity.ok(sensitiveWordService.updateSensitiveWord(id, request));
    }

    /**
     * Deletes a sensitive word.
     *
     * @param id id of the word to delete
     * @throws za.co.flash.sensitivewords.exception.SensitiveWordNotFoundException if no word has that id
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a sensitive word")
    @ApiResponse(responseCode = "204", description = "Word deleted")
    @ApiResponse(responseCode = "404", description = "Word not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> deleteSensitiveWord(
            @Parameter(description = "Id of the sensitive word", example = "1") @PathVariable Long id) {
        sensitiveWordService.deleteSensitiveWord(id);
        return ResponseEntity.noContent().build();
    }
}

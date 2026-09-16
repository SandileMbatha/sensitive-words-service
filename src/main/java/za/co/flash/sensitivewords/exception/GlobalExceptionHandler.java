package za.co.flash.sensitivewords.exception;

import za.co.flash.sensitivewords.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Central place that turns exceptions into consistent {@link ErrorResponse} responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @return 404 when a sensitive word id doesn't exist
     */
    @ExceptionHandler(SensitiveWordNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSensitiveWordNotFound(SensitiveWordNotFoundException notFoundException, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, notFoundException.getMessage(), request);
    }

    /**
     * @return 409 when a word being created or updated already exists
     */
    @ExceptionHandler(DuplicateSensitiveWordException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSensitiveWord(DuplicateSensitiveWordException duplicateException, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, duplicateException.getMessage(), request);
    }

    /**
     * @return 400 with each field's validation error joined into a single message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException validationException, HttpServletRequest request) {
        String message = validationException.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Fallback for anything unexpected, so callers always get a JSON {@link ErrorResponse} instead of a raw
     * stack trace. The real exception is left to the server logs rather than exposed to the caller.
     *
     * @return 500 with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception unexpectedException, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }
}

package za.co.flash.sensitivewords.exception;

import za.co.flash.sensitivewords.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
     * @return 405 when the path exists but does not support the HTTP method used, e.g. a GET on a path
     * that only accepts PUT/DELETE
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException methodNotSupportedException, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, methodNotSupportedException.getMessage(), request);
    }

    /**
     * Fallback for anything unexpected, so callers always get a JSON {@link ErrorResponse} instead of a raw
     * stack trace. The real exception is left to the server logs rather than exposed to the caller.
     *
     * @return 500 with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }
}

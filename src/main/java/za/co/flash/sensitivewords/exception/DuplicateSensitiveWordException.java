package za.co.flash.sensitivewords.exception;

/**
 * Thrown when a sensitive word is created or updated to a value that already exists.
 * Handled by {@link GlobalExceptionHandler}, which turns it into a 409 response.
 */
public class DuplicateSensitiveWordException extends RuntimeException {

    public DuplicateSensitiveWordException(String message) {
        super(message);
    }
}

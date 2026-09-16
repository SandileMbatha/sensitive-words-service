package za.co.flash.sensitivewords.exception;

/**
 * Thrown when a sensitive word id doesn't exist.
 * Handled by {@link GlobalExceptionHandler}, which turns it into a 404 response.
 */
public class SensitiveWordNotFoundException extends RuntimeException {

    public SensitiveWordNotFoundException(String message) {
        super(message);
    }
}

package pl.kocmon.booking.error;

import org.springframework.http.HttpStatus;

/**
 * Kody błędów aplikacji.
 * Każdy kod niesie odpowiadający mu status HTTP — dzięki temu handler
 * nie musi tego sprawdzać w if-else.
 */
public enum ErrorCode {

    // === Client errors (4xx) ===
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    BAD_REQUEST(HttpStatus.BAD_REQUEST),
    MALFORMED_JSON(HttpStatus.BAD_REQUEST),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),

    NOT_FOUND(HttpStatus.NOT_FOUND),
    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),

    // Domena bookingowa
    USER_NOT_FOUND(HttpStatus.NOT_FOUND),
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND),
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND),
    SEAT_ALREADY_TAKEN(HttpStatus.CONFLICT),
    EMAIL_ALREADY_USED(HttpStatus.CONFLICT),
    PAYMENT_FAILED(HttpStatus.UNPROCESSABLE_ENTITY),
    RESERVATION_EXPIRED(HttpStatus.GONE),

    CONFLICT(HttpStatus.CONFLICT),

    // === Server errors (5xx) ===
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

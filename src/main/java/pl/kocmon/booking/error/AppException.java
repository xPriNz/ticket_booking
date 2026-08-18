package pl.kocmon.booking.error;

/**
 * Bazowa klasa wszystkich wyjątków aplikacyjnych.
 * <p>
 * Rozszerza {@link RuntimeException} — świadomie unikamy checked exceptions,
 * żeby nie zaśmiecać sygnatur metod w warstwie serwisowej.
 * <p>
 * Każdy wyjątek niesie {@link ErrorCode} — kod używany przez klienta do
 * programowej reakcji na błąd (np. "USER_NOT_FOUND" → pokaż komunikat).
 */
public abstract class AppException extends RuntimeException {

    private final ErrorCode code;

    protected AppException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    protected AppException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}

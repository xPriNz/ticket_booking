package pl.kocmon.booking.error;

/**
 * Ogólny wyjątek reguł biznesowych — używany, gdy operacja narusza
 * warunek dziedzinowy, dla którego nie ma dedykowanej klasy.
 * <p>
 * Status HTTP wynika z {@link ErrorCode} przekazanego do konstruktora.
 * <p>
 * Przykład: {@code new BusinessException(ErrorCode.PAYMENT_FAILED, "Card declined")}
 */
public class BusinessException extends AppException {

    public BusinessException(ErrorCode code, String message) {
        super(code, message);
    }

    public BusinessException(ErrorCode code, String message, Throwable cause) {
        super(code, message, cause);
    }
}

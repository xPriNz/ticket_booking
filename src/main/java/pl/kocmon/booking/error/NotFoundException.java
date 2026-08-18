package pl.kocmon.booking.error;

/**
 * Zasób nie istnieje. Mapuje się na HTTP 404.
 * <p>
 * Preferuj konkretne factory (userNotFound, eventNotFound) zamiast
 * generycznego konstruktora — dostajesz spójne komunikaty i konkretne kody.
 */
public class NotFoundException extends AppException {

    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }

    public NotFoundException(ErrorCode code, String message) {
        super(code, message);
    }

    public static NotFoundException user(Long id) {
        return new NotFoundException(ErrorCode.USER_NOT_FOUND, "User " + id + " not found");
    }

    public static NotFoundException event(Long id) {
        return new NotFoundException(ErrorCode.EVENT_NOT_FOUND, "Event " + id + " not found");
    }

    public static NotFoundException seat(Long id) {
        return new NotFoundException(ErrorCode.SEAT_NOT_FOUND, "Seat " + id + " not found");
    }
}

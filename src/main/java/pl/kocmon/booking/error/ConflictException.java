package pl.kocmon.booking.error;

/**
 * Konflikt stanu — np. duplikat lub próba rezerwacji zajętego miejsca.
 * Mapuje się na HTTP 409.
 */
public class ConflictException extends AppException {

    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }

    public ConflictException(ErrorCode code, String message) {
        super(code, message);
    }

    public static ConflictException seatAlreadyTaken(Long seatId, Long eventId) {
        return new ConflictException(
            ErrorCode.SEAT_ALREADY_TAKEN,
            "Seat " + seatId + " for event " + eventId + " is already reserved"
        );
    }

    public static ConflictException emailAlreadyUsed(String email) {
        return new ConflictException(
            ErrorCode.EMAIL_ALREADY_USED,
            "Email " + email + " is already registered"
        );
    }
}

package pl.kocmon.booking.ReservationService.dtos.responses;

import pl.kocmon.booking.ReservationService.models.SeatStateMachine;

public record OccurrenceSeatResponse(
    long id,
    float x,
    float y,
    float rotationRadians,
    SeatStateMachine state
) {
    //SeatResponse() {} // TODO - constructor from model
}

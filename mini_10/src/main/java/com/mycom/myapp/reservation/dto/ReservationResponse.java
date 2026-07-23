package com.mycom.myapp.reservation.dto;

import com.mycom.myapp.reservation.entity.Reservation;
import com.mycom.myapp.reservation.entity.ReservationStatus;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long trainerScheduleId,
        Long ticketId,
        LocalDateTime reservedAt,
        ReservationStatus status
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getTrainerSchedule().getId(),
                reservation.getTicket().getId(),
                reservation.getReservedAt(),
                reservation.getStatus()
        );
    }
}

package com.mycom.myapp.reservation.dto;

import com.mycom.myapp.reservation.entity.Reservation;
import com.mycom.myapp.reservation.entity.ReservationStatus;
import java.time.LocalDateTime;

/** 트레이너가 보는 내 수업의 예약자 한 명. */
public record ScheduleReservationResponse(
        Long reservationId,
        Long memberId,
        String memberName,
        String memberEmail,
        LocalDateTime reservedAt,
        ReservationStatus status
) {
    public static ScheduleReservationResponse from(Reservation reservation) {
        return new ScheduleReservationResponse(
                reservation.getId(),
                reservation.getMember().getId(),
                reservation.getMember().getName(),
                reservation.getMember().getEmail(),
                reservation.getReservedAt(),
                reservation.getStatus()
        );
    }
}

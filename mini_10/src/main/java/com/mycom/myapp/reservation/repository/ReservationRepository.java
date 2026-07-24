package com.mycom.myapp.reservation.repository;

import com.mycom.myapp.reservation.entity.Reservation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByMemberIdAndTrainerScheduleId(Long memberId, Long trainerScheduleId);

    boolean existsByTrainerScheduleId(Long trainerScheduleId);

    Optional<Reservation> findByIdAndMemberId(Long reservationId, Long memberId);

    List<Reservation> findAllByMemberIdOrderByReservedAtDesc(Long memberId);

    // 트레이너 예약 현황: 특정 수업의 예약자 명단 (취소 이력 포함)
    List<Reservation> findAllByTrainerScheduleIdOrderByReservedAtAsc(Long trainerScheduleId);
}

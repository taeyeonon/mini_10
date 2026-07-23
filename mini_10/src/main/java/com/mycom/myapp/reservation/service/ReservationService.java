package com.mycom.myapp.reservation.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.common.InvalidOperationException;
import com.mycom.myapp.common.ResourceNotFoundException;
import com.mycom.myapp.reservation.dto.ReservationResponse;
import com.mycom.myapp.reservation.entity.Reservation;
import com.mycom.myapp.reservation.entity.ReservationStatus;
import com.mycom.myapp.reservation.repository.ReservationRepository;
import com.mycom.myapp.schedule.ScheduleStatus;
import com.mycom.myapp.schedule.TrainerSchedule;
import com.mycom.myapp.schedule.TrainerScheduleRepository;
import com.mycom.myapp.ticket.entity.Ticket;
import com.mycom.myapp.ticket.service.TicketService;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(readOnly = true)
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TrainerScheduleRepository scheduleRepository;
    private final TicketService ticketService;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            TrainerScheduleRepository scheduleRepository,
            TicketService ticketService,
            UserRepository userRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.scheduleRepository = scheduleRepository;
        this.ticketService = ticketService;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReservationResponse reserve(Long memberId, Long scheduleId) {
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다."));
        TrainerSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("수업 일정을 찾을 수 없습니다."));

        validateReservable(schedule);
        Reservation existing = reservationRepository
                .findByMemberIdAndTrainerScheduleId(memberId, scheduleId)
                .orElse(null);
        if (existing != null && existing.getStatus() == ReservationStatus.CONFIRMED) {
            throw new InvalidOperationException("이미 예약한 수업입니다.");
        }

        Ticket ticket = ticketService.useTicketAndGet(memberId);
        schedule.increaseReservedCount();

        Reservation reservation;
        if (existing == null) {
            reservation = new Reservation(member, schedule, ticket);
        } else {
            existing.confirm(ticket);
            reservation = existing;
        }
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse cancel(Long memberId, Long reservationId) {
        Reservation reservation = reservationRepository.findByIdAndMemberId(reservationId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("예약 내역을 찾을 수 없습니다."));
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new InvalidOperationException("이미 취소된 예약입니다.");
        }
        if (!reservation.getTrainerSchedule().getStartTime().isAfter(LocalDateTime.now())) {
        		throw new InvalidOperationException("이미 시작된 수업은 취소할 수 없습니다.");
        }

        ticketService.cancelTicket(reservation.getTicket());
        
        reservation.getTrainerSchedule().decreaseReservedCount();
        reservation.cancel();
        reservationRepository.save(reservation);
        
        return ReservationResponse.from(reservation);
    }

    public List<ReservationResponse> findMine(Long memberId) {
        return reservationRepository.findAllByMemberIdOrderByReservedAtDesc(memberId)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    private void validateReservable(TrainerSchedule schedule) {
        if (schedule.getStatus() != ScheduleStatus.OPEN) {
            throw new InvalidOperationException("예약 가능한 수업이 아닙니다.");
        }
        if (!schedule.getStartTime().isAfter(LocalDateTime.now())) {
            throw new InvalidOperationException("이미 시작된 수업은 예약할 수 없습니다.");
        }
        if (schedule.isFull()) {
            throw new InvalidOperationException("수업 정원이 모두 찼습니다.");
        }
    }
}

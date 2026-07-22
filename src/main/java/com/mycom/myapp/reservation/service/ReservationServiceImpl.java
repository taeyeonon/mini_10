package com.mycom.myapp.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.reservation.dto.ReservationDto;
import com.mycom.myapp.reservation.entity.Reservation;
import com.mycom.myapp.reservation.entity.ReservationStatus;
import com.mycom.myapp.reservation.exception.DuplicateReservationException;
import com.mycom.myapp.reservation.exception.ScheduleFullException;
import com.mycom.myapp.reservation.exception.ScheduleNotAvailableException;
import com.mycom.myapp.reservation.exception.TicketShortageException;
import com.mycom.myapp.schedule.entity.ScheduleStatus;
import com.mycom.myapp.schedule.entity.TrainerSchedule;
import com.mycom.myapp.schedule.repository.TrainerScheduleRepository;
import com.mycom.myapp.ticket.repository.TicketRepository;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService{

	private final ReservationService reservationService;
	private final TrainerScheduleRepository trainerScheduleRepository;
	private final TicketRepository ticketRepository;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public ReservationDto reserveSchedule(Long customerId, Long trainerScheduleId) {
		
		// 1단계 : 스케줄 조회 ( 비관적 락 : 동시 예약 방지 )
		TrainerSchedule schedule = trainerScheduleRepository.findByIdForUpdate(trainerScheduleId)
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스케줄입니다"));

		// 2단계 : 스케줄 상태 확인 (AVAILABLE이 아니면 예약 불가)
		if( schedule.getStatus() != ScheduleStatus.AVAILABLE ) {
			throw new ScheduleNotAvailableException();
		}

		// 3단계 : 정원 확인 ( 현재 예약 수 < 정원 )
		if( schedule.getReservationCount() >= schedule.getCapacity() ) {
			throw new ScheduleFullException();
		}

		// 4단계 : 중복 예약 확인
		//         같은 회원이 같은 스케줄을 이미 CONFIRMED 상태로 예약했는지
		reservationRepository.findByMemberIdAndTrainerScheduleId(customerId, trainerScheduleId)
					.ifPresent(existing -> {
						if(existing.getStatus() == ReservationStatus.CONFIRMED) {
							throw new DuplicateReservationException();
						}
					});

		// 5단계 : 사용할 수강권 조회 ( 비관적 락 )
		//         잔여 > 0, 만료 전인 수강권 중 만료 임박한 것부터 사용
		List<Ticket> usableTickets = ticketRepository.findUsableTicketsForUpdate(customerId, LocalDate.now());
		if( usableTickets.isEmpty() ) {
			throw new TicketShortageException();
		}
		Ticket ticket = usableTickets.get(0);

		// 6단계 : 예약 처리
		//         수강권 1회 차감 → 스케줄 예약 수 증가 → 예약 내역 저장
		ticket.setRemainingCount(ticket.getRemainingCount() - 1);
		schedule.setReservationCount(schedule.getReservationCount() + 1);

		User member = userRepository.getReferenceById(customerId);

		Reservation reservation = Reservation.builder()
					.member(member)
					.trainerSchedule(schedule)
					.ticket(ticket)
					.reservedAt(LocalDateTime.now())
					.status(ReservationStatus.CONFIRMED)
					.build();
		
		reservationRepository.save(reservation);

		return toDto(reservation);
	}

	// ═══════════════════════════════════════════════════════════════
	// 예약 취소 : 수강권 환급 + 스케줄 예약 수 감소
	// ═══════════════════════════════════════════════════════════════
	@Override
	@Transactional
	public ReservationDto cancelReservation(Long customerId, Long reservationId) {
		Reservation reservation = reservationRepository.findById(reservationId)
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다"));

		// 본인 예약만 취소 가능
		if( !reservation.getMember().getId().equals(customerId) ) {
			throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다");
		}
		if( reservation.getStatus() != ReservationStatus.CONFIRMED ) {
			throw new IllegalArgumentException("이미 취소된 예약입니다");
		}

		// 수강권 1회 환급
		Ticket ticket = reservation.getTicket();
		ticket.setRemainingCount(ticket.getRemainingCount() + 1);

		// 스케줄 예약 수 감소 (다른 회원의 예약에 영향 없음)
		TrainerSchedule schedule = reservation.getTrainerSchedule();
		schedule.setReservationCount(schedule.getReservationCount() - 1);

		reservation.setStatus(ReservationStatus.CANCELLED);

		log.info("예약 취소 : memberId={}, reservationId={}, 남은예약수={}", 
				memberId, reservationId, schedule.getReservationCount());

		return toDto(reservation);
	}

	@Override
	@Transactional(readOnly=true)
	public List<ReservationDto> getMyReservations(Long customerId) {
		return reservationService.getMyReservations(customerId);
	}

	private ReservationDto toDto(Reservation reservation) {
		TrainerSchedule schedule = reservation.getTrainerSchedule();
		return ReservationDto.builder()
					.id(reservation.getId())
					.trainerScheduleId(schedule.getId())
					.trainerName(schedule.getTrainer().getName())
					.startTime(schedule.getStartTime())
					.endTime(schedule.getEndTime())
					.ticketId(reservation.getTicket().getId())
					.reservedAt(reservation.getReservedAt())
					.status(reservation.getStatus().name())
					.build();
	}
}
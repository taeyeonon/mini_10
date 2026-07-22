package com.mycom.myapp.reservation.service;

import java.util.List;

import com.mycom.myapp.reservation.dto.ReservationDto;

public interface ReservationService {

	// 회원의 스케줄 예약
	ReservationDto reserveSchedule(Long customerId, Long trainerScheduleId);
	
	// 예약 취소
	ReservationDto cancelReservation(Long customerId, Long reservationId);
	
	// 예약 내역 조회
	List<ReservationDto> getMyReservations(Long customerId);
}

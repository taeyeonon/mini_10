package com.mycom.myapp.reservation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mycom.myapp.reservation.dto.ReservationDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService{

	@Override
	public ReservationDto reserveSchedule(Long memberId, Long trainerScheduleId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReservationDto cancelReservation(Long memberId, Long reservationId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ReservationDto> getMyReservations(Long memberId) {
		// TODO Auto-generated method stub
		return null;
	}

}

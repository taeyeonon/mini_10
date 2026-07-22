package com.mycom.myapp.reservation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.reservation.dto.ReservationDto;
import com.mycom.myapp.reservation.dto.ReservationResultDto;
import com.mycom.myapp.reservation.service.ReservationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	// ═══════════════════════════════════════════════
	// 예약 : POST /member/reservations/{scheduleId}
	// ═══════════════════════════════════════════════
	@PostMapping("/customer/reservations/{scheduleId}")
	public ResponseEntity<ReservationResultDto> reserveSchedule(
			@AuthenticationPrincipal MyUserDetails userDetails,
			@PathVariable("scheduleId") Long scheduleId
	) {
		ReservationDto reservationDto = reservationService.reserveSchedule(userDetails.getId(), scheduleId);
		
		ReservationResultDto resultDto = ReservationResultDto.builder()
					.result("success")
					.reservationDto(reservationDto)
					.build();
		
		return ResponseEntity.ok(resultDto);
	}

	// ═══════════════════════════════════════════════
	// 예약 취소 : DELETE /member/reservations/{id}
	// ═══════════════════════════════════════════════
	@DeleteMapping("/customer/reservations/{reservationId}")
	public ResponseEntity<ReservationResultDto> cancelReservation(
			@AuthenticationPrincipal MyUserDetails userDetails,
			@PathVariable("reservationId") Long reservationId
	) {
		ReservationDto reservationDto = reservationService.cancelReservation(userDetails.getId(), reservationId);
		
		ReservationResultDto resultDto = ReservationResultDto.builder()
					.result("success")
					.reservationDto(reservationDto)
					.build();
		
		return ResponseEntity.ok(resultDto);
	}

	// ═══════════════════════════════════════════════
	// 내 예약 조회 : GET /customer/reservations/my
	// ═══════════════════════════════════════════════
	@GetMapping("/customer/reservations/my")
	public ResponseEntity<ReservationResultDto> getMyReservations(@AuthenticationPrincipal MyUserDetails userDetails) {
		List<ReservationDto> list = reservationService.getMyReservations(userDetails.getId());
		
		ReservationResultDto resultDto = ReservationResultDto.builder()
					.result("success")
					.reservationDtoList(list)
					.build();
		
		return ResponseEntity.ok(resultDto);
	}
}
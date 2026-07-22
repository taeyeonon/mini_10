package com.mycom.myapp.user.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.reservation.dto.ReservationDto;
import com.mycom.myapp.user.dto.UserDto;
import com.mycom.myapp.user.dto.UserResultDto;
import com.mycom.myapp.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	// POST /users - 회원가입
	@PostMapping("")
	public UserResultDto insertUser(@RequestBody UserDto userDto) {
		return userService.insertUser(userDto);
	}

	// GET /customer/{id}/reservations - 내 예약 내역 조회
	@GetMapping("/reservations/my")
	public List<ReservationDto> getMyReservations(@PathVariable Long id) {
		return userService.getMyReservations(id);
	}

}

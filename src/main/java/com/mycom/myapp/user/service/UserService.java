package com.mycom.myapp.user.service;

import java.util.List;

import com.mycom.myapp.reservation.dto.ReservationDto;
import com.mycom.myapp.user.dto.UserDto;
import com.mycom.myapp.user.dto.UserResultDto;

public interface UserService {

	// 회원가입
	UserResultDto insertUser(UserDto userDto);

	// 로그인한 회원의 예약 내역 조회
	List<ReservationDto> getMyReservations(Long memberId);
	
	// 회원 정보 조회
	UserResultDto getUserById(Long id);
	
	
}

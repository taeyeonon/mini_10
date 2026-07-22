package com.mycom.myapp.user.service;

import java.util.List;

import com.mycom.myapp.reservation.dto.ReservationDto;
import com.mycom.myapp.user.dto.UserDto;
import com.mycom.myapp.user.dto.UserResultDto;

public interface UserService {

	// 회원가입
	UserResultDto insertUser(UserDto userDto);
	
}

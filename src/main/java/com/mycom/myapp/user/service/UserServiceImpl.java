package com.mycom.myapp.user.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.mycom.myapp.reservation.dto.ReservationDto;
import com.mycom.myapp.reservation.entity.Reservation;
import com.mycom.myapp.reservation.repository.ReservationRepository;
import com.mycom.myapp.user.dto.UserDto;
import com.mycom.myapp.user.dto.UserResultDto;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.entity.UserRole;
import com.mycom.myapp.user.repository.UserRepository;
import com.mycom.myapp.user.repository.UserRoleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{

	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	private final ReservationRepository reservationRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public UserResultDto insertUser(UserDto userDto) {
		UserResultDto userResultDto = new UserResultDto();

		try {
			// 1단계 : 이메일 중복 검사 ( email 은 로그인 식별자 )
			if( userRepository.findByEmail(userDto.getEmail()).isPresent() ) {
				userResultDto.setResult("duplicated");
				return userResultDto;
			}

			// 2단계 : 기본 권한 MEMBER 조회
			List<UserRole> userRoles = List.of(userRoleRepository.findByName("CUSTOMER"));

			// 3단계 : 비밀번호 암호화 + User 엔티티 생성
			User user = User.builder()
							.name(userDto.getName())
							.email(userDto.getEmail())
							.password(passwordEncoder.encode(userDto.getPassword()))
							.userRoles(userRoles)
							.build();

			// 4단계 : DB 저장 ( user, user_user_role 매핑 테이블 동시 저장 )
			User savedUser = userRepository.save(user);
			log.info("회원 가입 완료 : {}", savedUser.getEmail());

			userResultDto.setResult("success");
		}catch(Exception e) {
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			userResultDto.setResult("fail");
			log.error("회원 가입 실패", e);
		}

		return userResultDto;

	}

}

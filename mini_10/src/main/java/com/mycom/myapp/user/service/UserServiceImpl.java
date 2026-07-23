package com.mycom.myapp.user.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.mycom.myapp.user.dto.UserDto;
import com.mycom.myapp.user.dto.UserResultDto;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.entity.UserRole;
import com.mycom.myapp.user.repository.UserRepository;
import com.mycom.myapp.user.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{

	// 사용자 등록할 때 CUSTOMER Role 부여 
	
	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	
	// 사용자 입력 패스워드 (일반 텍스트) 암호화 후 저장
	// 내맘대로 암호화가 아니라 현재 프로젝트에 설정된 암호화 객체 이용 => DI
	private final PasswordEncoder passwordEncoder;
	
	@Override
	public UserResultDto insertUser(UserDto userDto) {
		UserResultDto userResultDto = new UserResultDto();
		
		try {
			List<String> requestedRoles = userDto.getRoles();
			if (requestedRoles == null || requestedRoles.isEmpty()) {
				requestedRoles = List.of("CUSTOMER");
			}
			
			List<UserRole> userRoles = requestedRoles.stream()
					.map(String::trim)
					.map(String::toUpperCase)
					.distinct()
					.map(roleName -> {
						UserRole userRole = userRoleRepository.findByName(roleName);
						if (userRole == null) {
							throw new IllegalArgumentException("지원하지 않는 역할입니다: " + roleName);
						}
						return userRole;
					})
					.toList();
			
			// 비밀번호 암호화 후 사용자 정보 저장
			User user = User.builder()
							.name(userDto.getName())
							.email(userDto.getEmail())
							.password(passwordEncoder.encode(userDto.getPassword()))
							.userRoles(userRoles)
							.build();
			User savedUser = userRepository.save(user);
			log.debug("User Registerd: {}", savedUser.getEmail());
			
			userResultDto.setResult("success");
		}catch(Exception e) {
			e.printStackTrace();
			// 트랜잭션 롤백 (사용자와 권한 정보 insert 실패 시)
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			userResultDto.setResult("fail");
		}
		
		return userResultDto;
	}

}











package com.mycom.myapp.auth.service;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.mycom.myapp.auth.dto.LoginResultDto;
import com.mycom.myapp.jwt.JwtUtil;
import com.mycom.myapp.user.dto.UserDto;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService{

	// 인증 처리
	private final AuthenticationManager authenticationManager;
	
	// 인증 성공 시 JWT 토큰 발급
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	
	@Override
	public LoginResultDto login(String email, String password) {
		LoginResultDto loginResultDto = new LoginResultDto();
		
		try {
			// AuthenticationManager에 인증 요청
			// - DaoAuthenticationProvider가 MyUserDetailsService.loadUserByUsername() 호출
			// - 입력 비밀번호와 DB 저장된 비밀번호 매칭 확인
			Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(email, password)
			);
			
			// 인증 성공 -> JWT 발급
			String username = authentication.getName();
			List<String> roles = authentication.getAuthorities().stream()
									.map(GrantedAuthority::getAuthority).toList();
			
			String token = jwtUtil.createToken(username, roles);
			User user = userRepository.findByEmail(username)
					.orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));
			UserDto userDto = new UserDto();
			userDto.setId(user.getId());
			userDto.setEmail(user.getEmail());
			userDto.setName(user.getName());
			userDto.setRoles(roles.stream().map(role -> role.startsWith("ROLE_") ? role.substring(5) : role).toList());
			loginResultDto.setResult("success");
			loginResultDto.setToken(token);
			loginResultDto.setUserDto(userDto);
			log.info("Login successed for {}", email);
		}catch(Exception e) {
			loginResultDto.setResult("fail");
			log.warn("Login failed for {}", email);
		}
		
		return loginResultDto;
	}

}
















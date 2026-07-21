package com.mycom.myapp.config;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.entity.UserRole;
import com.mycom.myapp.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService{

	private final UserRepository userRepository;
	
	// 로그인 시 이메일과 비밀번호로 사용자 인증
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		Optional<User> optionalUser = userRepository.findByEmail(email);
		
		if( optionalUser.isPresent() ) {
			User user = optionalUser.get();
			List<UserRole> listUserRole = user.getUserRoles();
			
			// Spring Security의 hasRole() 규격에 맞게 ROLE_ 접두사 추가
			List<SimpleGrantedAuthority> authorities = listUserRole.stream()
														.map(UserRole::getName)
														.map(name -> "ROLE_" + name)
														.map(SimpleGrantedAuthority::new)
														.toList();
			return MyUserDetails.builder()
					.username(user.getEmail())  	
					.password(user.getPassword()) 	
					.authorities(authorities) 		
					.id(user.getId())
					.name(user.getName())
					.email(user.getEmail())
					.build();
		}
		
		throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
	}


}
